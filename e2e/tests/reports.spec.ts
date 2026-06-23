import { test } from '../fixtures/gymFixture';
import { createMember, createMembership, createPlan } from '../utils/apiClient';
import { MembersPage } from '../pages/MembersPage';
import { MemberDetailPage, PaymentFormPage } from '../pages/MemberDetailPage';
import { MorePage, ReportsPage } from '../pages/MorePage';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

test.describe('Reports', () => {
  test('revenue report reflects a payment recorded this month', async ({ authenticatedPage: page, gym }) => {
    const member = await createMember(gym.adminToken, gym.branchId, {
      fullName: `Report Target ${Date.now()}`,
      phone: '5557778888',
    });
    const plan = await createPlan(gym.adminToken, {
      name: `Report Plan E2E ${Date.now()}`,
      planType: 'MONTHLY',
      durationDays: 30,
      price: 65,
    });
    await createMembership(gym.adminToken, { memberId: member.id, planId: plan.id, startDate: todayIso() });

    await page.getByText('Members', { exact: true }).first().click();
    await new MembersPage(page).openMember(member.id);
    const detailPage = new MemberDetailPage(page);
    await detailPage.openRecordPayment();

    const paymentForm = new PaymentFormPage(page);
    await paymentForm.fillAmount('65');
    await paymentForm.selectPaymentType('Full');
    await paymentForm.selectPaymentMethod('Card');
    await paymentForm.submit();

    await page.getByText('More', { exact: true }).first().click();
    const morePage = new MorePage(page);
    await morePage.goToReports();

    const reportsPage = new ReportsPage(page);
    await reportsPage.expectRevenueTotal('$65.00');
  });
});
