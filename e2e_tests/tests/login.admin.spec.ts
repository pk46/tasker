import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';

test.describe("login actions", () => {
    let loginPage: LoginPage;
    let dashboardPage: DashboardPage;

    test.beforeEach(async ({ page }) => {
        loginPage = new LoginPage(page);
        dashboardPage = new DashboardPage(page);
        await loginPage.goto();
    });

    test("homepage is loaded", async ({ page }) => {
        await expect(page).toHaveTitle("frontend");
        await expect(page).toHaveURL(/.*#\/login/);
        await expect(loginPage.mainTitle).toHaveText("Task Management");
    });

    test("admin can log in and logout", async ({ page }) => {
        await loginPage.loginAdmin();

        await expect(page).toHaveURL(/.*#\/$/);
        await expect(dashboardPage.dashboardTitle).toHaveText("Dashboard");

        await dashboardPage.logout();

        await expect(page).toHaveURL(/.*#\/login/);
        await expect(loginPage.mainTitle).toHaveText("Task Management");
    });
});

