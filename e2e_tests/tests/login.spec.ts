import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { UserCreateDTO, UserRole } from '../lib/api/UsersApi';
import { test, expect } from '../lib/fixtures/authenticated';
import { createDefaultUserDto, createEdituserDto, generateUniqueEmail, generateUniqueUsername } from '../lib/helpers/testDataGenerator';
import { UsersPage } from '../pages/UsersPage';
import { EditUserParams } from '../pages/modals/CreateUserModal';

test.describe("login actions", () => {
    let loginPage: LoginPage;
    let dashboardPage: DashboardPage;
    let userPage: UsersPage;

    test.beforeEach(async ({ page }) => {
        loginPage = new LoginPage(page);
        dashboardPage = new DashboardPage(page);
        userPage = new UsersPage(page);
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
        expect(await dashboardPage.getUserRole()).toBe("ADMIN");

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
            firstName: "Toprak",
            lastName: "Razgatlioglu"
        });

        try {
            await loginPage.login(username, password);

            await expect(page).toHaveURL(/.*#\/$/);
            await expect(dashboardPage.dashboardTitle).toHaveText("Dashboard");
            expect(await dashboardPage.getUserRole()).toBe("USER");
            expect (await dashboardPage.getUserName()).toBe(`${user.firstName} ${user.lastName}`);

            await dashboardPage.logout();

            await expect(page).toHaveURL(/.*#\/login/);
            await expect(loginPage.mainTitle).toHaveText("Task Management");
        } finally {
            await usersApi.delete(user.id);
        }
    })

    test("non existent user can't login", async ({ page }) => {
        const username = "nonexistentuser";
        const password = "autotestPass!@#"

        await loginPage.login(username, password);

        await expect(page).toHaveURL(/.*#\/login/);
        await expect(loginPage.mainTitle).toHaveText("Task Management")

    })

    test("user with wrong password can't login", async ({ page, usersApi }) => {
        const username = generateUniqueUsername("login");
        const email = generateUniqueEmail();
        
        const userCreatDto: UserCreateDTO = {
            username: username,
            email: email,
            role: UserRole.ADMIN,
            password: "Test123/"
        }

        try {
            await usersApi.create(userCreatDto);
            await loginPage.login(username, "abc987/");

            await expect(page).toHaveURL(/.*#\/login/);
            await expect(loginPage.mainTitle).toHaveText("Task Management")
        
        } finally {
            await usersApi.deleteByEmail(email);
        }
    })

    test("user with changed password can login", async ({ page, usersApi }) => {
        const userData = createDefaultUserDto();
        const editParams: EditUserParams = createEdituserDto(
            {
                password: "NewPassword123",
                userRole: UserRole.ADMIN
            }        
        );

        await loginPage.loginAdmin();
        await dashboardPage.goToPage("Users");

        try {
            await userPage.createNewUser(userData);
            await userPage.editUser(editParams, userData.username)
            
            await dashboardPage.logout();

            await loginPage.login(userData.username, editParams.password!);

            await expect(page).toHaveURL(/.*#\/$/);
            await expect(dashboardPage.dashboardTitle).toHaveText("Dashboard");
        
        } finally {
            await usersApi.deleteByEmail(userData.email);
        }
        
    })

    test("user with changed password can't login with an old password", async ({ page, usersApi }) => {
        const userData = createDefaultUserDto();
        const editParams: EditUserParams = createEdituserDto(
            {
                password: "NewPassword123",
                userRole: UserRole.ADMIN
            }        
        );

        await loginPage.loginAdmin();
        await dashboardPage.goToPage("Users");

        try {
            await userPage.createNewUser(userData);
            await userPage.editUser(editParams, userData.username)
            
            await dashboardPage.logout();

            await loginPage.login(userData.username, userData.password!);
           
            await expect(page).toHaveURL(/.*#\/login/);
            await expect(loginPage.mainTitle).toHaveText("Task Management")
        
        } finally {
            await usersApi.deleteByEmail(userData.email);
        }
        
    })
});

