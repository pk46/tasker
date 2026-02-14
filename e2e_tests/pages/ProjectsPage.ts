import { Page, Locator } from '@playwright/test';
import { BasePage } from './BasePage';
import { CreateProjectModal, CreateProjectParams, EditProjectParams } from './modals/CreateProjectModal';

export type ProjectDetails = {
    name: string,
    description: string,
    owner: string,
    created: string,
    tasks: number,
}

export class ProjectsPage extends BasePage {
    readonly createProjectModal: CreateProjectModal;
    readonly createProjectButton: Locator;

    constructor(page: Page) {
        super(page);
        this.createProjectModal = new CreateProjectModal(page);
        this.createProjectButton = page.getByRole('button', { name: 'Create Project' });
    }

    async createProject(projectParams: CreateProjectParams) {
        await this.createProjectButton.click();
        await this.createProjectModal.waitForModal();
        await this.createProjectModal.createProject(projectParams);
    }

    async editProject(projectName: string, editParams: EditProjectParams) {
        const project = this.getProject(projectName);
        const editButton = project.locator('//button[contains(., "Edit")]');

        await editButton.click();
        await this.createProjectModal.waitForModal();
        await this.createProjectModal.editProject(editParams);
    }

    async deleteProject(projectName: string) {
        const project = this.getProject(projectName);
        const deleteButton = project.getByRole('button', { name: 'Delete' });

        this.page.once('dialog', async dialog => {
            await dialog.accept();
        });

        await deleteButton.click();
        await project.waitFor({ state: 'detached' });
    }

    async isProjectPresent(projectName: string): Promise<boolean> {
        const project = this.getProject(projectName);
        return await project.count() > 0;
    }

    async getProjectData(projectName: string): Promise<ProjectDetails> {
        const project = this.getProject(projectName);

        const nameElement = project.locator('h3');
        const descriptionElement = project.locator('p').first();
        const ownerElement = project.locator('span').filter({ hasText: /Owner:|by/ }).first();
        const createdElement = project.locator('span').filter({ hasText: /Created:|on/ }).first();
        const tasksElement = project.locator('span').filter({ hasText: /Tasks?/ }).first();

        const name = await nameElement.textContent();
        const description = await descriptionElement.textContent();
        const owner = await ownerElement.textContent();
        const created = await createdElement.textContent();
        const tasksText = await tasksElement.textContent();

        const tasksCount = tasksText ? parseInt(tasksText.match(/\d+/)?.[0] || '0') : 0;

        return {
            name: name?.trim() || '',
            description: description?.trim() || '',
            owner: owner?.trim().replace(/^Owner:|^by\s*/i, '').trim() || '',
            created: created?.trim().replace(/^Created:|^on\s*/i, '').trim() || '',
            tasks: tasksCount,
        };
    }

    async getProjectName(projectName: string): Promise<string> {
        const project = this.getProject(projectName);
        const nameElement = project.locator('h3');
        return (await nameElement.textContent())?.trim() || '';
    }

    async getProjectDescription(projectName: string): Promise<string> {
        const project = this.getProject(projectName);
        const descriptionElement = project.locator('p').first();
        return (await descriptionElement.textContent())?.trim() || '';
    }

    private getProject(projectName: string): Locator {
        return this.page.locator(`//h3[text()='${projectName}']/ancestor::div[2]`);
    }
}