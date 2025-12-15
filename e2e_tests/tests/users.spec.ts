import { test, expect } from '../lib/fixtures/authenticated';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { UsersPage, ColumnNames } from '../pages/UsersPage';
import { UserRole } from '../lib/api/UsersApi';
import { createDefaultUserDto, createEdituserDto } from '../lib/helpers/testDataGenerator';
import { EditUserParams } from '../pages/modals/CreateUserModal';


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
        await dashboardPage.goToUsersPage();
    });

    test.afterEach(async ({ usersApi }, testInfo) => {
        const emailAttachment = testInfo.attachments.find(a => a.name === 'userEmail');
        if (emailAttachment?.body) {
            try {
                await usersApi.deleteByEmail(emailAttachment.body.toString());
            } catch (error) {
                console.error(`Failed to delete user ${emailAttachment.body.toString()}`);
            }
        }
    });

    test("create new user", async ({}, testInfo) => {
        const userData = createDefaultUserDto();

        testInfo.attach('userEmail', { body: userData.email });

        await userPage.createNewUser(userData)

        expect(await userPage.getUserCellValue(userData.username, ColumnNames.email)).toBe(userData.email);
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.username)).toBe(userData.username);
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.role)).toBe(UserRole.USER);
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.actions)).toContain("Edit")
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.actions)).toContain("Delete")
    })

    test("delete user", async ({}) => {
        const userData = createDefaultUserDto();

        await userPage.createNewUser(userData);
        await userPage.deleteUser(userData.email);

        expect(await userPage.isUserPresent(userData.username)).toBe(false);
    })

    test("edit user", async({}, testInfo) => {
        const userData = createDefaultUserDto();

        await userPage.createNewUser(userData)

        expect(await userPage.getUserCellValue(userData.username, ColumnNames.email)).toBe(userData.email);
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.username)).toBe(userData.username);
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.role)).toBe(UserRole.USER);
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.actions)).toContain("Edit")
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.actions)).toContain("Delete")

        const editParams: EditUserParams = createEdituserDto(
            {
            firstName: "John",
            lastName: "Edited",
            userRole: UserRole.ADMIN
            }
        )

        await userPage.editUser(editParams, userData.email)

        expect(await userPage.getUserCellValue(userData.username, ColumnNames.email)).toBe(editParams.email);
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.name)).toBe(`${editParams.firstName} ${editParams.lastName}`);
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.role)).toBe(UserRole.ADMIN);
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.actions)).toContain("Edit")
        expect(await userPage.getUserCellValue(userData.username, ColumnNames.actions)).toContain("Delete")

        testInfo.attach('userEmail', { body: editParams.email });
    })
});