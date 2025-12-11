import { Page, Locator } from '@playwright/test';
import { UserRole } from '../lib/api/UsersApi';
import { CreateUserModal } from './modals/CreateUserModal';

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
}