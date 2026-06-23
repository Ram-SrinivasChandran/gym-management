import { defineConfig, devices } from '@playwright/test';
import { API_BASE_URL, WEB_BASE_URL } from './utils/env';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 4 : undefined,
  reporter: [
    ['html', { open: 'never', outputFolder: 'playwright-report' }],
    ['list'],
  ],
  timeout: 30_000,
  expect: { timeout: 10_000 },

  use: {
    baseURL: WEB_BASE_URL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 10_000,
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  // Spins up the dockerized backend and the Expo web build automatically when not
  // already running (e.g. local dev). In CI, both are started as explicit prior steps
  // and reuseExistingServer keeps Playwright from double-starting them.
  webServer: [
    {
      command: 'docker compose up --build -d',
      cwd: '..',
      url: `${API_BASE_URL}/actuator/health`,
      timeout: 180_000,
      reuseExistingServer: true,
    },
    {
      command: 'npm run web',
      cwd: '../mobile',
      url: WEB_BASE_URL,
      timeout: 120_000,
      reuseExistingServer: true,
      env: { CI: '1' },
    },
  ],
});
