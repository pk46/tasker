import { Page, Locator } from '@playwright/test';
import { UserRole } from '../../lib/api/UsersApi';

export class CreateUserModal {
    readonly page: Page;
    readonly modal: Locator;
    readonly usernameInput: Locator;
    readonly emailInput: Locator;
    readonly passwordInput: Locator;
    readonly firstNameInput: Locator;
    readonly lastNameInput: Locator;
    readonly roleDropdown: Locator;
    readonly saveButton: Locator;
    readonly cancelButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.modal = page.locator('[role="dialog"]');
        this.usernameInput = this.modal.getByLabel('Username');
        this.emailInput = this.modal.getByLabel('Email');
        this.passwordInput = this.modal.getByLabel('Password');
        this.firstNameInput = this.modal.getByLabel('First Name');
        this.lastNameInput = this.modal.getByLabel('Last Name');
        this.roleDropdown = this.modal.getByLabel('Role');
        this.saveButton = this.modal.getByRole('button', { name: 'Create User' });
        this.cancelButton = this.modal.getByRole('button', { name: 'Cancel' });
    }

    async waitForModal() {
        await this.modal.waitFor({ state: 'visible' });
    }

    async createUser(username: string, email: string, password: string, role: UserRole, firstName?: string, lastName?: string) {
        await this.waitForModal();

        const fields = [
            { locator: this.usernameInput, value: username },
            { locator: this.emailInput, value: email },
            { locator: this.passwordInput, value: password },
            { locator: this.firstNameInput, value: firstName },
            { locator: this.lastNameInput, value: lastName }
        ];

        for (const field of fields) {
            if (field.value) {
                await field.locator.fill(field.value);
            }
        }

        await this.roleDropdown.selectOption(role);
        await this.saveButton.click();
        await this.modal.waitFor({ state: 'hidden' });
    }

    async close() {
        await this.cancelButton.click();
        await this.modal.waitFor({ state: 'hidden' });
    }
}