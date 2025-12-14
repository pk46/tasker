import { test, expect } from '../lib/fixtures/authenticated';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { UsersPage, ColumnNames } from '../pages/UsersPage';
import { UserRole } from '../lib/api/UsersApi';
import { generateUniqueEmail, generateUniqueUsername } from '../lib/helpers/testDataGenerator';


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

    test.afterEach(async ({ usersApi }, testInfo) => {
        if (testInfo.tags.includes('@cleanup')) {
            const email = (testInfo as any).createdUserEmail;
            if (email) {
                try {
                    await usersApi.deleteByEmail(email);
                } catch (error) {
                    console.error(`Failed to delete user ${email}`)
                }
            }
        }
    })

    test("create new user @cleanup", async ({}, testInfo) => {
        await dashboardPage.goToUser();
        const username = generateUniqueUsername("users");
        const email = generateUniqueEmail();

        (testInfo as any).createdUserEmail = email;

        await userPage.createNewUser(username, email, "autotestpass", UserRole.USER)

        expect(await userPage.getUserCellValue(email, ColumnNames.email)).toBe(email);
        expect(await userPage.getUserCellValue(email, ColumnNames.username)).toBe(username);
        expect(await userPage.getUserCellValue(email, ColumnNames.role)).toBe(UserRole.USER);
        expect(await userPage.getUserCellValue(email, ColumnNames.actions)).toContain("Edit")
        expect(await userPage.getUserCellValue(email, ColumnNames.actions)).toContain("Delete")
    })

});