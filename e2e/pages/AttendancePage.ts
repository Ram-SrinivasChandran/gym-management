import { Page, expect } from '@playwright/test';

export class AttendancePage {
  constructor(private readonly page: Page) {}

  async search(query: string) {
    await this.page.getByTestId('attendance-search-input').fill(query);
  }

  async checkIn(memberId: string) {
    await this.page.getByTestId(`checkin-${memberId}`).click();
  }

  async checkOut(memberId: string) {
    await this.page.getByTestId(`checkout-${memberId}`).click();
  }

  async expectToast(text: string) {
    await expect(this.page.getByTestId('global-toast')).toContainText(text);
  }
}
