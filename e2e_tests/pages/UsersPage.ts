import { Page, Locator } from '@playwright/test';
import { CreateUserModal, CreateUserParams, EditUserParams } from './modals/CreateUserModal';
import { BasePage } from './BasePage';

export enum ColumnNames {
    id = "Id",
    username = "Username",
    email = "Email",
    name = "Name",
    role = "Role",
    actions = "Actions"
}

export class UsersPage extends BasePage {
    readonly createEditUserModal: CreateUserModal;
    readonly mainTitle: Locator;
    private readonly newUserButton: Locator;

    constructor(page: Page) {
        super(page);
        this.createEditUserModal = new CreateUserModal(page);
        this.mainTitle = this.page.getByRole('heading', { name: 'Users' })
        this.newUserButton = this.page.getByRole('button', { name: 'Create User' });
    }

    async createNewUser(createuserParams: CreateUserParams) {
        await this.newUserButton.click();
        await this.createEditUserModal.waitForModal();
        await this.createEditUserModal.createUser(createuserParams);
    }

    async deleteUser(userEmail: string): Promise<void> {
        const userRow = await this.getUserRow(userEmail);
        const deleteButton = userRow.getByRole('button', { name: 'Delete' });

        this.page.once('dialog', async dialog => {
            await dialog.accept();
        });

        await deleteButton.click();
        await userRow.waitFor({ state: 'detached' });
    }

    async editUser(editParams: EditUserParams, userName: string): Promise<void> {
        const userRow = await this.getUserRow(userName);
        const editButton = userRow.getByRole('button', { name: 'Edit' });
        
        await editButton.click();
        await this.createEditUserModal.editUser(editParams)
    }

    async getUserCellValue(userName: string, columnName: ColumnNames): Promise<string> {
        const userRow = await this.getUserRow(userName);
        const columnIndex = await this.getColumnIndex(columnName);
        const cell = userRow.locator(`td:nth-child(${columnIndex})`);
        const cellValue = await cell.textContent();

        if (cellValue === null || cellValue === undefined) {
            throw new Error(`Cell value not found for user ${userName} in column ${columnName}`);
        }

        return cellValue;
    }

    async isUserPresent(userName: string): Promise<boolean> {
        const userRow = this.page.getByRole('row').filter({
            has: this.page.getByRole('cell', { name: userName, exact: true })
        });
        return await userRow.count() > 0;
    }

    private async getUserRow(userName: string): Promise<Locator> {
        return this.page.getByRole('row').filter({
            has: this.page.getByRole('cell', { name: userName })
        });
    }

    private async getColumnIndex(columnName: string): Promise<number> {
        const headerCells = this.page.locator('thead th');
        const count = await headerCells.count();

        for (let i = 0; i < count; i++) {
            const headerText = await headerCells.nth(i).textContent();
            if (headerText?.trim() === columnName) {
                return i + 1;
            }
        }

        throw new Error(`Column with name "${columnName}" not found`);
    }

}