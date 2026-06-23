import { Page, expect } from '@playwright/test';

export class MemberDetailPage {
  constructor(private readonly page: Page) {}

  async expectBmi(value: string) {
    await expect(this.page.getByText(`BMI: ${value}`)).toBeVisible();
  }

  async openAddMembership() {
    await this.page.getByTestId('add-membership-button').click();
  }

  async openRecordPayment() {
    await this.page.getByTestId('record-payment-button').click();
  }

  async openPaymentHistory() {
    await this.page.getByTestId('payment-history-button').click();
  }

  async checkIn() {
    await this.page.getByTestId('detail-check-in-button').click();
  }

  async checkOut() {
    await this.page.getByTestId('detail-check-out-button').click();
  }

  async expectStatus(status: string) {
    await expect(this.page.getByTestId('membership-status-badge').getByText(status)).toBeVisible();
  }

  async expectPendingAmount(text: string) {
    await expect(this.page.getByTestId('pending-amount-text')).toContainText(text);
  }
}

export class MembershipFormPage {
  constructor(private readonly page: Page) {}

  async selectPlan(planName: string) {
    await this.page.getByTestId('membership-plan-picker').click();
    await this.page.getByText(planName, { exact: false }).first().click();
  }

  async setStartDate(isoDate: string) {
    const input = this.page.getByTestId('membership-start-date-input');
    await input.fill('');
    await input.fill(isoDate);
  }

  async submit() {
    await this.page.getByTestId('create-membership-button').click();
  }
}

export class PaymentFormPage {
  constructor(private readonly page: Page) {}

  async fillAmount(amount: string) {
    await this.page.getByTestId('payment-amount-input').fill(amount);
  }

  async selectPaymentType(type: 'Full' | 'Partial' | 'Advance') {
    await this.page.getByText(type, { exact: true }).click();
  }

  async selectPaymentMethod(method: 'Cash' | 'Card' | 'UPI' | 'Bank' | 'Other') {
    await this.page.getByText(method, { exact: true }).click();
  }

  async submit() {
    await this.page.getByTestId('submit-payment-button').click();
  }
}

export class PaymentHistoryPage {
  constructor(private readonly page: Page) {}

  async expectPaymentVisible(amountText: string) {
    await expect(this.page.getByText(amountText).first()).toBeVisible();
  }
}
