import { Page, expect } from '@playwright/test';

export class MorePage {
  constructor(private readonly page: Page) {}

  async goToPlans() {
    await this.page.getByTestId('more-nav-plans').click();
  }

  async goToReports() {
    await this.page.getByTestId('more-nav-reports').click();
  }

  async goToNotifications() {
    await this.page.getByTestId('more-nav-notifications').click();
  }

  async logout() {
    await this.page.getByTestId('logout-button').click();
  }
}

export class PlansPage {
  constructor(private readonly page: Page) {}

  async createPlan(fields: { name: string; durationDays: string; price: string }) {
    await this.page.getByTestId('plan-name-input').fill(fields.name);
    await this.page.getByTestId('plan-duration-input').fill(fields.durationDays);
    await this.page.getByTestId('plan-price-input').fill(fields.price);
    await this.page.getByTestId('add-plan-button').click();
  }

  async expectPlanVisible(name: string) {
    await expect(this.page.getByText(name).first()).toBeVisible();
  }
}

export class ReportsPage {
  constructor(private readonly page: Page) {}

  async expectRevenueTotal(amountText: string) {
    await expect(this.page.getByTestId('revenue-total-value')).toHaveText(amountText);
  }
}

export class NotificationsPage {
  constructor(private readonly page: Page) {}

  async broadcast(message: string) {
    await this.page.getByTestId('broadcast-message-input').fill(message);
    await this.page.getByTestId('send-broadcast-button').click();
  }

  async expectNotificationTypeVisible(type: string) {
    await expect(this.page.getByText(type).first()).toBeVisible();
  }
}
