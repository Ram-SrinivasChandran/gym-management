import { test, expect } from '../fixtures/gymFixture';
import { createMember } from '../utils/apiClient';
import { AttendancePage } from '../pages/AttendancePage';

test.describe('Attendance', () => {
  test('checks a member in and out from the front-desk attendance screen', async ({
    authenticatedPage: page,
    gym,
  }) => {
    const member = await createMember(gym.adminToken, gym.branchId, {
      fullName: `Attendance Target ${Date.now()}`,
      phone: '5550001111',
    });

    await page.getByText('Attendance', { exact: true }).first().click();

    const attendancePage = new AttendancePage(page);
    await attendancePage.search(member.memberCode);
    await expect(page.getByText(member.memberCode)).toBeVisible();

    await attendancePage.checkIn(member.id);
    await attendancePage.expectToast('Checked in');

    await attendancePage.checkOut(member.id);
    await attendancePage.expectToast('Checked out');
  });

  test('rejects a second check-in for the same member on the same day', async ({
    authenticatedPage: page,
    gym,
  }) => {
    const member = await createMember(gym.adminToken, gym.branchId, {
      fullName: `Duplicate Checkin Target ${Date.now()}`,
      phone: '5552221111',
    });

    await page.getByText('Attendance', { exact: true }).first().click();
    const attendancePage = new AttendancePage(page);
    await attendancePage.search(member.memberCode);

    await attendancePage.checkIn(member.id);
    await attendancePage.expectToast('Checked in');

    await attendancePage.checkIn(member.id);
    await attendancePage.expectToast('Check-in failed');
  });
});
