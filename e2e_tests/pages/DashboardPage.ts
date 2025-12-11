import { Page, Locator } from '@playwright/test';

export class DashboardPage {
    readonly page: Page;
    readonly dashboardTitle: Locator;
    readonly logoutButton: Locator;
    readonly usersButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.dashboardTitle = page.getByRole('heading', { name: 'Dashboard' });
        this.logoutButton = page.getByRole('button', { name: 'Logout' });
        this.usersButton = page.locator("//nav//a[text()='Users']")
    }

    async isLoaded() {
        return await this.dashboardTitle.isVisible();
    }

    async getTitle() {
        return await this.dashboardTitle.textContent();
    }

    async logout() {
        await this.logoutButton.click();
    }

    async goToUser() {
        await this.usersButton.click();
        await this.page.waitForURL(/.*#\/users/);
    }
}
