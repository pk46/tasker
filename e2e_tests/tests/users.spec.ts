import { expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { UsersPage } from '../pages/UsersPage';
import { UserRole } from '../lib/api/UsersApi';
import { test } from '../lib/fixtures/userHelper';


test.describe("User tests", () => {
    let loginPage: LoginPage;
    let dashboardPage: DashboardPage;
    let userPage: UsersPage

    test.beforeEach(async ({ page }) => {
        loginPage = new LoginPage(page);
        dashboardPage = new DashboardPage(page);
        userPage = new UsersPage(page);

        await loginPage.goto();
        await loginPage.loginAdmin();
    });

    test("create new user", async ({ page, uniqueEmail, uniqueUsername }) => {
        await dashboardPage.goToUser();
        await userPage.createNewUser(uniqueUsername, uniqueEmail, "autotestpass", UserRole.USER )
    })

});
