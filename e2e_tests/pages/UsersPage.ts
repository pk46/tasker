import { Page, Locator } from '@playwright/test';
import { UserRole } from '../lib/api/UsersApi';
import { CreateUserModal } from './modals/CreateUserModal';

export enum ColumnNames {
    id = "Id",
    username = "Username",
    email = "Email",
    name = "Name",
    role = "Role",
    actions = "Actions"
}

export class UsersPage {
    readonly page: Page
    readonly createUserModal: CreateUserModal;
    readonly mainTitle: Locator;
    private readonly newUserButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.createUserModal = new CreateUserModal(page);
        this.mainTitle = this.page.getByRole('heading', { name: 'Users' })
        this.newUserButton = this.page.getByRole('button', { name: 'Create User' });
    }

    async createNewUser(username: string, email: string, password: string, role: UserRole, firstName?: string, lastName?: string) {
        await this.newUserButton.click();
        await this.createUserModal.waitForModal();
        await this.createUserModal.createUser(username, email, password, role, firstName, lastName);
    }

    async getUserCellValue(userEmail: string, columnName: ColumnNames): Promise<string> {
        const userRow = await this.getUserRow(userEmail);
        const columnIndex = await this.getColumnIndex(columnName);
        const cell = userRow.locator(`td:nth-child(${columnIndex})`);
        const cellValue = await cell.textContent();

        if (cellValue === null || cellValue === undefined) {
            throw new Error(`Cell value not found for user ${userEmail} in column ${columnName}`);
        }

        return cellValue;
    }

    private async getUserRow(userEmail: string): Promise<Locator> {
        return this.page.getByRole('row').filter({
            has: this.page.getByRole('cell', { name: userEmail })
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