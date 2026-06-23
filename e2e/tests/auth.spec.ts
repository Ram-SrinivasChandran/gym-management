import { test, expect } from '../fixtures/gymFixture';
import { LoginPage } from '../pages/LoginPage';
import { MorePage } from '../pages/MorePage';

test.describe('Authentication', () => {
  test('gym admin can log in with valid credentials and reach the dashboard', async ({ page, gym }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(gym.adminEmail, gym.adminPassword);

    await expect(page.getByTestId('stat-value-total-members')).toBeVisible();
  });

  test('shows an error for invalid credentials and does not navigate', async ({ page, gym }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(gym.adminEmail, 'WrongPassword123!');

    await loginPage.expectLoginError('Invalid email or password');
    await expect(page.getByTestId('login-submit-button')).toBeVisible();
  });

  test('rejects an unknown email', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login('nobody@e2e.test', 'WhateverPassword1!');

    await loginPage.expectLoginError('Invalid email or password');
  });

  test('logging out returns to the login screen', async ({ authenticatedPage: page }) => {
    const morePage = new MorePage(page);
    await page.getByText('More', { exact: true }).first().click();
    await morePage.logout();

    await expect(page.getByTestId('login-email-input')).toBeVisible();
  });
});
