import { Page, expect } from '@playwright/test';

export class MembersPage {
  constructor(private readonly page: Page) {}

  async search(query: string) {
    await this.page.getByTestId('members-search-input').fill(query);
  }

  async openAddMemberForm() {
    await this.page.getByTestId('add-member-fab').click();
  }

  async openMember(memberId: string) {
    await this.page.getByTestId(`member-card-${memberId}`).click();
  }

  async expectMemberVisible(fullName: string) {
    await expect(this.page.getByText(fullName).first()).toBeVisible();
  }
}

export class MemberFormPage {
  constructor(private readonly page: Page) {}

  async selectBranch(branchName: string) {
    await this.page.getByTestId('branch-picker').click();
    await this.page.getByText(branchName, { exact: true }).click();
  }

  async fillProfile(fields: {
    fullName: string;
    phone: string;
    email?: string;
    heightCm?: string;
    weightKg?: string;
  }) {
    await this.page.getByTestId('member-fullname-input').fill(fields.fullName);
    await this.page.getByTestId('member-phone-input').fill(fields.phone);
    if (fields.email) {
      await this.page.getByTestId('member-email-input').fill(fields.email);
    }
    if (fields.heightCm) {
      await this.page.getByTestId('member-height-input').fill(fields.heightCm);
    }
    if (fields.weightKg) {
      await this.page.getByTestId('member-weight-input').fill(fields.weightKg);
    }
  }

  async save() {
    await this.page.getByTestId('save-member-button').click();
  }
}
