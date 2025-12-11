import { Page, Locator } from '@playwright/test';

const ADMIN_USERNAME = process.env.ADMIN_USERNAME!;
const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD!;

export class LoginPage {
    readonly page: Page;
    readonly usernameInput: Locator;
    readonly passwordInput: Locator;
    readonly signInButton: Locator;
    readonly mainTitle: Locator;

    constructor(page: Page) {
        this.page = page;
        this.usernameInput = page.getByLabel('Username');
        this.passwordInput = page.getByLabel('Password');
        this.signInButton = page.locator('button[type="submit"]');
        this.mainTitle = page.getByRole('heading', { name: 'Task Management' });
    }

    async goto() {
        await this.page.goto('/#/login');
    }

    async login(username: string, password: string) {
        await this.usernameInput.fill(username);
        await this.passwordInput.fill(password);
        await this.signInButton.click();
    }

    async loginAdmin() {
        await this.usernameInput.fill(ADMIN_USERNAME);
        await this.passwordInput.fill(ADMIN_PASSWORD);
        await this.signInButton.click();
    }

    async getTitle() {
        return await this.mainTitle.textContent();
    }
}
