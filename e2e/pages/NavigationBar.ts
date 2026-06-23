import { Page } from '@playwright/test';

/** Bottom tab bar present on every authenticated screen. */
export class NavigationBar {
  constructor(private readonly page: Page) {}

  async goToDashboard() {
    await this.page.getByText('Dashboard', { exact: true }).first().click();
  }

  async goToMembers() {
    await this.page.getByText('Members', { exact: true }).first().click();
  }

  async goToAttendance() {
    await this.page.getByText('Attendance', { exact: true }).first().click();
  }

  async goToMore() {
    await this.page.getByText('More', { exact: true }).first().click();
  }
}
