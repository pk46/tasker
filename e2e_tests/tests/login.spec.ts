import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { UserRole } from '../lib/api/UsersApi';
import { test, expect } from '../lib/fixtures/authenticated';
import { generateUniqueEmail, generateUniqueUsername } from '../lib/helpers/testDataGenerator';

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

    test("non admin user can login and logout", async ({ page, usersApi }) => {
        const username = generateUniqueUsername("login");
        const email = generateUniqueEmail();
        const password = "autotestPass!@#";

        const user = await usersApi.create({
            username,
            email,
            password,
            role: UserRole.USER,
        });

        try {
            await loginPage.login(username, password);

            await expect(page).toHaveURL(/.*#\/$/);
            await expect(dashboardPage.dashboardTitle).toHaveText("Dashboard");

            await dashboardPage.logout();

            await expect(page).toHaveURL(/.*#\/login/);
            await expect(loginPage.mainTitle).toHaveText("Task Management");
        } finally {
            await usersApi.delete(user.id);
        }
    })
});

