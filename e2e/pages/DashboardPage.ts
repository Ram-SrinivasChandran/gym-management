import { Page, expect } from '@playwright/test';

export class DashboardPage {
  constructor(private readonly page: Page) {}

  async expectLoaded() {
    await expect(this.page.getByText('Dashboard', { exact: true }).first()).toBeVisible();
    await expect(this.page.getByTestId('stat-value-total-members')).toBeVisible();
  }

  async getStatValue(slug: string): Promise<string> {
    return (await this.page.getByTestId(`stat-value-${slug}`).textContent()) ?? '';
  }
}
