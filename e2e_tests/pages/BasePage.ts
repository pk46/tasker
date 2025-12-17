import { Page, Locator } from '@playwright/test';

export class BasePage {
    readonly page: Page;
    readonly logoutButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.logoutButton = page.getByRole('button', { name: 'Logout' });
    }

    async logout() {
        await this.logoutButton.click();
    }

    async goToUsersPage() {
        const usersButton = this.page.locator("//nav//a[text()='Users']");
        await usersButton.click();
        await this.page.waitForURL(/.*#\/users/);
    }

    async getPageTitle(titleLocator: Locator): Promise<string | null> {
        return await titleLocator.textContent();
    }

    async isPageLoaded(titleLocator: Locator): Promise<boolean> {
        return await titleLocator.isVisible();
    }
}
