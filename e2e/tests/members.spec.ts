import { test, expect } from '../fixtures/gymFixture';
import { MembersPage, MemberFormPage } from '../pages/MembersPage';
import { MemberDetailPage } from '../pages/MemberDetailPage';

test.describe('Member Management', () => {
  test('creates a member via the UI and finds them by search', async ({ authenticatedPage: page, gym }) => {
    await page.getByText('Members', { exact: true }).first().click();

    const membersPage = new MembersPage(page);
    await membersPage.openAddMemberForm();

    const formPage = new MemberFormPage(page);
    await formPage.selectBranch('Main Branch');
    const fullName = `Alex Johnson ${Date.now()}`;
    await formPage.fillProfile({ fullName, phone: '5551234567', email: 'alex.johnson@e2e.test' });
    await formPage.save();

    await membersPage.search(fullName);
    await membersPage.expectMemberVisible(fullName);
  });

  test('computes BMI on the member detail screen', async ({ authenticatedPage: page }) => {
    await page.getByText('Members', { exact: true }).first().click();

    const membersPage = new MembersPage(page);
    await membersPage.openAddMemberForm();

    const formPage = new MemberFormPage(page);
    await formPage.selectBranch('Main Branch');
    const fullName = `BMI Test ${Date.now()}`;
    await formPage.fillProfile({ fullName, phone: '5559876543', heightCm: '180', weightKg: '81' });
    await formPage.save();

    await page.getByText(fullName).first().click();

    const detailPage = new MemberDetailPage(page);
    await detailPage.expectBmi('25');
  });
});
