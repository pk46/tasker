import { Page, Locator } from '@playwright/test';
import { BasePage } from './BasePage';

export class DashboardPage extends BasePage {
    readonly dashboardTitle: Locator;
    readonly usersCount: Locator;

    constructor(page: Page) {
        super(page);
        this.dashboardTitle = page.getByRole('heading', { name: 'Dashboard' });
        this.usersCount = page.getByTestId("total-users")
    }

    async isLoaded() {
        return await this.isPageLoaded(this.dashboardTitle);
    }

    async getTitle() {
        return await this.getPageTitle(this.dashboardTitle);
    }

    async getUsersCount(): Promise<number> {
        const count = await this.usersCount.textContent();
        return Number(count)
    }
}
