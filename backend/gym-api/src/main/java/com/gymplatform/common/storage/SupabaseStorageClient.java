package com.gymplatform.common.storage;

import com.gymplatform.common.exception.BadRequestException;
import java.time.Instant;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Uploads files to Supabase Storage using the service_role key, which bypasses Storage's
 * row-level security entirely. Used server-side only: the mobile app's direct-upload path
 * (client JWT + RLS policies) proved unreliable for this project, so photo uploads are
 * proxied through this backend instead.
 */
@Component
public class SupabaseStorageClient {

    private final RestClient restClient;
    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucket;

    public SupabaseStorageClient(@Value("${supabase.url}") String supabaseUrl,
                                  @Value("${supabase.storage.service-role-key}") String serviceRoleKey,
                                  @Value("${supabase.storage.member-photos-bucket}") String bucket) {
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
        this.bucket = bucket;
        this.restClient = RestClient.create();
    }

    /**
     * Uploads a member's profile photo and returns its public URL. The path is namespaced by
     * memberId with a timestamp suffix so re-uploads don't collide with (or get served stale
     * via CDN cache from) the previous photo.
     */
    public String uploadMemberPhoto(java.util.UUID memberId, byte[] bytes, String contentType) {
        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            throw new BadRequestException("Photo upload is not configured on this server.");
        }
        String extension = extensionFor(contentType);
        String path = "%s/%d.%s".formatted(memberId, Instant.now().toEpochMilli(), extension);

        String uploadUrl = "%s/storage/v1/object/%s/%s".formatted(supabaseUrl, bucket, path);
        try {
            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("x-upsert", "true")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new BadRequestException("Failed to upload photo: " + ex.getMessage());
        }

        return "%s/storage/v1/object/public/%s/%s".formatted(supabaseUrl, bucket, path);
    }

    private String extensionFor(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        String subtype = contentType.toLowerCase(Locale.ROOT).replace("image/", "");
        return subtype.isBlank() ? "jpg" : subtype;
    }
}
