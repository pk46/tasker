import { Page, Locator } from '@playwright/test';
import { UserRole } from '../../lib/api/UsersApi';

export type CreateUserParams = {
    username: string,
    email: string,
    password: string,
    userRole: UserRole,
    firstName?: string,
    lastName?: string
}

export type EditUserParams = {
    email?: string,
    password?: string,
    userRole?: UserRole,
    firstName?: string,
    lastName?: string
}

export class CreateUserModal {
    readonly page: Page;
    readonly modal: Locator;
    readonly usernameInput: Locator;
    readonly emailInput: Locator;
    readonly passwordInput: Locator;
    readonly firstNameInput: Locator;
    readonly lastNameInput: Locator;
    readonly roleDropdown: Locator;
    readonly createButton: Locator;
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
        this.createButton = this.modal.getByRole('button', { name: 'Create User' });
        this.cancelButton = this.modal.getByRole('button', { name: 'Cancel' });
        this.saveButton = this.modal.getByRole('button', { name: 'Save Changes' });
    }

    async waitForModal() {
        await this.modal.waitFor({ state: 'visible' });
    }

    async createUser(params: CreateUserParams) {
        await this.waitForModal();

        const fields = [
            { locator: this.usernameInput, value: params.username },
            { locator: this.emailInput, value: params.email },
            { locator: this.passwordInput, value: params.password },
            { locator: this.firstNameInput, value: params.firstName },
            { locator: this.lastNameInput, value: params.lastName }
        ];

        for (const field of fields) {
            if (field.value) {
                await field.locator.fill(field.value);
            }
        }

        await this.roleDropdown.selectOption(params.userRole);
        await this.createButton.click();
        await this.modal.waitFor({ state: 'hidden' });
    }

    async editUser(editParams: EditUserParams) {
        const fields = [
            { locator: this.emailInput, value: editParams.email},
            { locator: this.passwordInput, value: editParams.password },
            { locator: this.firstNameInput, value: editParams.firstName },
            { locator: this.lastNameInput, value: editParams.lastName }
        ]

        for (const field of fields) {
            if (field.value) {
                await field.locator.fill(field.value);
            }
        }

        if (editParams.userRole) {
             await this.roleDropdown.selectOption(editParams.userRole);
        }
        
        await this.saveButton.click();
        await this.modal.waitFor({ state: 'hidden' });
    }

    async close() {
        await this.cancelButton.click();
        await this.modal.waitFor({ state: 'hidden' });
    }
}