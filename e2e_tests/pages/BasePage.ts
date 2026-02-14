import { Page, Locator } from '@playwright/test';

export class BasePage {
    readonly page: Page;
    readonly logoutButton: Locator;
    readonly userRole: Locator;
    readonly userName: Locator;

    constructor(page: Page) {
        this.page = page;
        this.logoutButton = page.getByRole('button', { name: 'Logout' });
        this.userRole = page.getByTestId("userrole");
        this.userName = page.getByTestId("username");
    }

    async logout() {
        await this.logoutButton.click();
    }

    async goToPage(pageName: string) {
        const usersButton = this.page.locator(`//nav//a[text()='${pageName}']`);
        await usersButton.click();
    }

    async getPageTitle(titleLocator: Locator): Promise<string> {
        const title = await titleLocator.textContent();

        if (!title) throw new Error("Couldn't get page title");

        return title;
    }

    async isPageLoaded(titleLocator: Locator): Promise<boolean> {
        return await titleLocator.isVisible();
    }

    async getUserRole(): Promise<string> {
        const role = await this.userRole.textContent();

        if (!role) throw new Error("Couldn't get user role");

        return role;
    }

    async getUserName(): Promise<string> {
        const name = await this.userName.textContent();

        if (!name) throw new Error("Couldn't get user name");

        return name;
    }
}
