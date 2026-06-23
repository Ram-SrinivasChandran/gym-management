import { test, expect } from '../fixtures/gymFixture';
import { createMember, createMembership, createPlan } from '../utils/apiClient';
import { MembersPage } from '../pages/MembersPage';
import { MemberDetailPage, PaymentFormPage, PaymentHistoryPage } from '../pages/MemberDetailPage';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

test.describe('Payments', () => {
  test('records a partial payment and shows the remaining pending amount', async ({
    authenticatedPage: page,
    gym,
  }) => {
    const member = await createMember(gym.adminToken, gym.branchId, {
      fullName: `Payment Target ${Date.now()}`,
      phone: '5552223333',
    });
    const planName = `Quarterly E2E ${Date.now()}`;
    const plan = await createPlan(gym.adminToken, {
      name: planName,
      planType: 'QUARTERLY',
      durationDays: 90,
      price: 150,
    });
    await createMembership(gym.adminToken, { memberId: member.id, planId: plan.id, startDate: todayIso() });

    await page.getByText('Members', { exact: true }).first().click();
    await new MembersPage(page).openMember(member.id);

    const detailPage = new MemberDetailPage(page);
    await detailPage.openRecordPayment();

    const paymentForm = new PaymentFormPage(page);
    await paymentForm.fillAmount('50');
    await paymentForm.selectPaymentType('Partial');
    await paymentForm.selectPaymentMethod('Cash');
    await paymentForm.submit();

    await detailPage.expectPendingAmount('$100.00');
  });

  test('payment history lists a recorded payment with its receipt details', async ({
    authenticatedPage: page,
    gym,
  }) => {
    const member = await createMember(gym.adminToken, gym.branchId, {
      fullName: `History Target ${Date.now()}`,
      phone: '5554445555',
    });
    const plan = await createPlan(gym.adminToken, {
      name: `Monthly History E2E ${Date.now()}`,
      planType: 'MONTHLY',
      durationDays: 30,
      price: 80,
    });
    await createMembership(gym.adminToken, { memberId: member.id, planId: plan.id, startDate: todayIso() });

    await page.getByText('Members', { exact: true }).first().click();
    await new MembersPage(page).openMember(member.id);

    const detailPage = new MemberDetailPage(page);
    await detailPage.openRecordPayment();

    const paymentForm = new PaymentFormPage(page);
    await paymentForm.fillAmount('80');
    await paymentForm.selectPaymentType('Full');
    await paymentForm.selectPaymentMethod('UPI');
    await paymentForm.submit();

    await detailPage.openPaymentHistory();
    const historyPage = new PaymentHistoryPage(page);
    await historyPage.expectPaymentVisible('$80.00');
    await expect(page.getByText('FULL · UPI', { exact: false })).toBeVisible();
  });
});
