import { Page, Locator } from '@playwright/test';

export type CreateProjectParams = {
    name: string,
    description?: string,
    owner: string,
}

export type EditProjectParams = {
    name?: string,
    description?: string,
}

export class CreateProjectModal {
    readonly page: Page;
    readonly modal: Locator;
    readonly name: Locator;
    readonly description: Locator;
    readonly ownerDropdown: Locator;
    readonly createButton: Locator;
    readonly saveButton: Locator;
    readonly cancelButton: Locator;


    constructor(page: Page) {
        this.page = page;
        this.modal = page.locator('[role="dialog"]');
        this.name = this.modal.getByLabel('Project Name');
        this.description = this.modal.getByLabel('Description');
        this.ownerDropdown = this.modal.getByLabel('Owner');
        this.createButton = this.modal.getByRole('button', { name: 'Create Project' });
        this.cancelButton = this.modal.getByRole('button', { name: 'Cancel' });
        this.saveButton = this.modal.getByRole('button', { name: 'Save Changes' });
        
    }

    async waitForModal() {
        await this.modal.waitFor({ state: 'visible' });
    }

    async createProject(params: CreateProjectParams) {
        await this.waitForModal();

        const fields = [
            { locator: this.name, value: params.name },
            { locator: this.description, value: params.description }
        ];

        for (const field of fields) {
            if (field.value) {
                await field.locator.fill(field.value);
            }
        }

        await this.ownerDropdown.selectOption(params.owner);
        await this.createButton.click();
        await this.modal.waitFor({ state: 'hidden' });
    }

    async editProject(editParams: EditProjectParams) {
        const fields = [
            { locator: this.name, value: editParams.name },
            { locator: this.description, value: editParams.description }
        ];

        for (const field of fields) {
            if (field.value) {
                await field.locator.fill(field.value);
            }
        }
        
        await this.saveButton.click();
        await this.modal.waitFor({ state: 'hidden' });
    }

    async close() {
        await this.cancelButton.click();
        await this.modal.waitFor({ state: 'hidden' });
    }
}