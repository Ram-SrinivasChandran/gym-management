# Production Deployment Guide — AWS

This guide stands up the backend (`backend/gym-api`) on AWS ECS Fargate with RDS PostgreSQL,
behind an Application Load Balancer, matching the topology in [ARCHITECTURE.md](ARCHITECTURE.md).
The mobile app is distributed via Expo/EAS (App Store / Play Store / web), not this infra.

## 1. Prerequisites

- AWS account with admin access to create IAM roles, VPC resources, ECS, RDS, S3, ECR, CloudWatch.
- AWS CLI v2 configured locally (`aws configure`) for the one-time bootstrap steps below.
- A GitHub repository with this codebase, Actions enabled, and access to configure repo
  Environments/Secrets/Variables (Settings → Environments → `production`).
- Domain name (optional but recommended) for the API, managed in Route 53 or elsewhere.

## 2. One-time AWS infrastructure setup

These are the resources the CI/CD pipeline (`.github/workflows/ci.yml`) deploys *into* — it does
not create them. Provision once, manually or via your IaC tool of choice (Terraform/CDK/CloudFormation
recommended for anything beyond a first deploy; raw CLI steps shown here for clarity).

### 2.1 Networking
- VPC with at least 2 public subnets (ALB) and 2 private subnets (ECS tasks, RDS), across 2 AZs.
- NAT gateway (or NAT instance) so private-subnet ECS tasks can reach the internet (e.g. for
  pulling images from ECR is fine via VPC endpoint instead — recommended to avoid NAT cost; pulling
  from package repositories at runtime is not needed since the Docker image is self-contained).
- Security groups:
  - `alb-sg`: inbound 443/80 from `0.0.0.0/0`, outbound to `ecs-sg` on port 8080.
  - `ecs-sg`: inbound 8080 from `alb-sg` only, outbound to `rds-sg` on 5432 and to the internet (or
    VPC endpoints) for ECR/CloudWatch.
  - `rds-sg`: inbound 5432 from `ecs-sg` only.

### 2.2 RDS PostgreSQL
```bash
aws rds create-db-instance \
  --db-instance-identifier gym-platform-prod \
  --db-instance-class db.t4g.medium \
  --engine postgres \
  --engine-version 16 \
  --master-username gymadmin \
  --master-user-password "<store-in-secrets-manager-instead>" \
  --allocated-storage 50 \
  --storage-type gp3 \
  --multi-az \
  --vpc-security-group-ids <rds-sg-id> \
  --db-subnet-group-name <private-subnet-group> \
  --backup-retention-period 7 \
  --no-publicly-accessible
```
- Store the master password in **AWS Secrets Manager** immediately after creation; never leave it
  in shell history or a `.env` file.
- Enable automated backups (7+ days) and consider a read replica once read traffic on
  dashboard/report endpoints grows.

### 2.3 Secrets Manager
Create one secret holding everything the API needs at runtime:
```bash
aws secretsmanager create-secret \
  --name gym-platform/prod/api \
  --secret-string '{
    "DB_URL": "jdbc:postgresql://<rds-endpoint>:5432/gymdb",
    "DB_USERNAME": "gymadmin",
    "DB_PASSWORD": "<rds-master-password>",
    "JWT_SECRET": "<generate-a-real-32+-byte-random-secret>"
  }'
```
Generate `JWT_SECRET` with `openssl rand -base64 48` — never reuse the `dev-only-secret-key...`
default from `application.yml`. **Do not** set `SPRING_PROFILES_ACTIVE=seed` or any
`GYMPLATFORM_SEED_SUPER_ADMIN_*` variables in production — that bootstrap path is for local
dev/E2E only (see [`SuperAdminSeeder`](../backend/gym-api/src/main/java/com/gymplatform/auth/bootstrap/SuperAdminSeeder.java)).
Create the first real Super Admin via a one-off `INSERT` against the production database instead,
with a properly bcrypt-hashed password, then rotate it on first login.

### 2.4 S3 (documents, profile photos, report exports)
```bash
aws s3api create-bucket --bucket gym-platform-prod-assets --region <region>
aws s3api put-bucket-encryption --bucket gym-platform-prod-assets \
  --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
aws s3api put-public-access-block --bucket gym-platform-prod-assets \
  --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
```
Member documents/photos and generated PDF/Excel reports should be stored here behind pre-signed
URLs — the application currently stores `profile_photo_url` / `file_url` as plain strings, so wire
your S3 upload flow to populate those fields with the bucket's object URL or a pre-signed URL.

### 2.5 ECR
```bash
aws ecr create-repository --repository-name gym-api --image-scanning-configuration scanOnPush=true
```

### 2.6 ECS Cluster, Task Definition, Service
```bash
aws ecs create-cluster --cluster-name gym-platform-prod
```

Task definition (`task-definition.json`) — register once; CI/CD registers new revisions on every
deploy by swapping only the `image` field (see `.github/workflows/ci.yml`'s `deploy` job):
```json
{
  "family": "gym-api",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "<ecsTaskExecutionRole-arn>",
  "taskRoleArn": "<ecsTaskRole-arn>",
  "containerDefinitions": [
    {
      "name": "gym-api",
      "image": "<ecr-repo-uri>:bootstrap",
      "portMappings": [{ "containerPort": 8080, "protocol": "tcp" }],
      "secrets": [
        { "name": "DB_URL", "valueFrom": "arn:aws:secretsmanager:<region>:<account>:secret:gym-platform/prod/api:DB_URL::" },
        { "name": "DB_USERNAME", "valueFrom": "arn:aws:secretsmanager:<region>:<account>:secret:gym-platform/prod/api:DB_USERNAME::" },
        { "name": "DB_PASSWORD", "valueFrom": "arn:aws:secretsmanager:<region>:<account>:secret:gym-platform/prod/api:DB_PASSWORD::" },
        { "name": "JWT_SECRET", "valueFrom": "arn:aws:secretsmanager:<region>:<account>:secret:gym-platform/prod/api:JWT_SECRET::" }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/gym-api",
          "awslogs-region": "<region>",
          "awslogs-stream-prefix": "gym-api"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "wget -q --spider http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 40
      }
    }
  ]
}
```
```bash
aws logs create-log-group --log-group-name /ecs/gym-api
aws ecs register-task-definition --cli-input-json file://task-definition.json
aws ecs create-service \
  --cluster gym-platform-prod \
  --service-name gym-api \
  --task-definition gym-api \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[<private-subnet-ids>],securityGroups=[<ecs-sg-id>],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=<target-group-arn>,containerName=gym-api,containerPort=8080" \
  --health-check-grace-period-seconds 60
```

### 2.7 ALB + Target Group
- Target group: protocol HTTP, port 8080, target type `ip`, health check path `/actuator/health`.
- ALB listener on 443 (ACM certificate) forwarding to the target group; redirect 80 → 443.

### 2.8 IAM roles
- `ecsTaskExecutionRole`: AWS-managed `AmazonECSTaskExecutionRolePolicy` plus
  `secretsmanager:GetSecretValue` scoped to the `gym-platform/prod/*` secret ARNs.
- `ecsTaskRole`: `s3:GetObject`/`PutObject` scoped to the assets bucket (only if the app talks to
  S3 directly rather than via pre-signed URLs generated elsewhere).
- A dedicated **deploy role** for GitHub Actions (OIDC, no long-lived keys — see §3).

## 3. GitHub Actions → AWS trust (OIDC, no static credentials)

Create an IAM OIDC identity provider for `token.actions.githubusercontent.com` (one-time per
account), then a role GitHub Actions can assume:

```bash
aws iam create-role --role-name gha-gym-platform-deploy \
  --assume-role-policy-document file://trust-policy.json
```

`trust-policy.json`:
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "Federated": "arn:aws:iam::<account>:oidc-provider/token.actions.githubusercontent.com" },
    "Action": "sts:AssumeRoleWithWebIdentity",
    "Condition": {
      "StringEquals": { "token.actions.githubusercontent.com:aud": "sts.amazonaws.com" },
      "StringLike": { "token.actions.githubusercontent.com:sub": "repo:<org>/<repo>:ref:refs/heads/main" }
    }
  }]
}
```

Attach a policy permitting only what the `deploy` job needs: `ecr:GetAuthorizationToken`,
`ecr:BatchCheckLayerAvailability`, `ecr:PutImage`, `ecr:InitiateLayerUpload`,
`ecr:UploadLayerPart`, `ecr:CompleteLayerUpload`, `ecs:DescribeTaskDefinition`,
`ecs:RegisterTaskDefinition`, `ecs:UpdateService`, `ecs:DescribeServices`, and
`iam:PassRole` scoped to the two ECS roles above.

## 4. GitHub repository configuration

In **Settings → Environments**, create a `production` environment with:
- Required reviewers (manual approval gate before `deploy` runs).
- Environment variables (`vars.*`, not secrets — these aren't sensitive):
  - `AWS_REGION`
  - `ECR_REGISTRY` (e.g. `<account>.dkr.ecr.<region>.amazonaws.com`)
  - `ECS_CLUSTER` = `gym-platform-prod`
  - `ECS_SERVICE` = `gym-api`
  - `ECS_TASK_FAMILY` = `gym-api`
- Environment secret:
  - `AWS_DEPLOY_ROLE_ARN` = `arn:aws:iam::<account>:role/gha-gym-platform-deploy`

With this, `.github/workflows/ci.yml`'s `deploy` job only runs on push to `main`, after lint, unit
tests, integration tests, E2E, security scan, and Docker build have all passed, and only after a
human approves the `production` environment gate.

## 5. Database migrations in production

Flyway runs automatically on application startup (`spring.flyway.enabled=true` in
`application.yml`) against whatever `DB_URL` the running task resolves from Secrets Manager. This
means:
- New ECS tasks apply pending migrations before serving traffic.
- During a rolling deploy, **the old and new task versions briefly run side-by-side** — write
  migrations that are backward-compatible with the previous app version (additive columns,
  nullable-then-backfill, no destructive drops in the same release that still has old tasks
  reading the old shape).
- Never run `mvn flyway:clean` or anything destructive against `gym-platform/prod/api`.

## 6. Observability

- **CloudWatch Logs**: `/ecs/gym-api` log group (configured above) captures stdout/stderr,
  including the structured Spring Boot startup logs and Hibernate SQL (keep `logging.level` at
  `INFO` in prod — the `application.yml` default already avoids SQL logging at INFO).
- **CloudWatch Alarms**: alarm on ALB `TargetGroup` `UnHealthyHostCount` > 0 for 2+ minutes, RDS
  `FreeStorageSpace` and `CPUUtilization`, and ECS service `CPUUtilization`/`MemoryUtilization`.
- **Actuator health**: `/actuator/health` is the only exposed management endpoint
  (`management.endpoints.web.exposure.include: health` in `application.yml`) — used by both the
  ALB target group and the container `HEALTHCHECK` in the Dockerfile. Do not widen this exposure
  without adding authentication to the management port.

## 7. Rollback

ECS retains prior task definition revisions. To roll back:
```bash
aws ecs update-service \
  --cluster gym-platform-prod \
  --service gym-api \
  --task-definition gym-api:<previous-revision-number>
aws ecs wait services-stable --cluster gym-platform-prod --services gym-api
```
Database migrations are not automatically rolled back — this is why §5's backward-compatibility
rule matters; a rollback should never require an un-migration.

## 8. Mobile app distribution (not part of this AWS infra)

The mobile app (`mobile/`, Expo) ships independently of the backend:
- **Web**: `npx expo export --platform web` produces a static bundle deployable to S3 + CloudFront
  or any static host, pointed at the production API via `EXPO_PUBLIC_API_BASE_URL`.
- **iOS/Android**: use EAS Build (`eas build --platform all`) and EAS Submit for App Store /
  Play Store releases. Configure `app.json`'s `extra.apiBaseUrl` (or the `EXPO_PUBLIC_API_BASE_URL`
  env var read in `src/api/config.js`) to point at the production ALB domain per build profile
  (development/preview/production).
