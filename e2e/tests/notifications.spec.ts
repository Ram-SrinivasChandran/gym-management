import { test } from '../fixtures/gymFixture';
import { MorePage, NotificationsPage } from '../pages/MorePage';

test.describe('Notifications', () => {
  test('gym admin can broadcast a promotional notification', async ({ authenticatedPage: page }) => {
    await page.getByText('More', { exact: true }).first().click();
    const morePage = new MorePage(page);
    await morePage.goToNotifications();

    const notificationsPage = new NotificationsPage(page);
    await notificationsPage.broadcast('50% off personal training this week!');

    await notificationsPage.expectNotificationTypeVisible('PROMOTIONAL');
  });
});
