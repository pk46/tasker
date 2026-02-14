import { test, expect } from '../lib/fixtures/authenticated';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { creatDefaultProjectDto } from '../lib/helpers/testDataGenerator';
import { ProjectsPage } from '../pages/ProjectsPage';

test.describe("Project tests", () => {
    let loginPage: LoginPage;
    let dashboardPage: DashboardPage;
    let projectsPage: ProjectsPage;

    test.beforeEach(async ({ page }, testInfo) => {
        loginPage = new LoginPage(page);
        dashboardPage = new DashboardPage(page);
        projectsPage = new ProjectsPage(page);

        await loginPage.goto();
        await loginPage.loginAdmin();

        const projectsCount = await dashboardPage.getProjectsCount();
        testInfo.attach('projectsCount', { body: projectsCount!.toString() });

        await dashboardPage.goToPage("Projects");
    });

    test.afterEach(async ({ projectsApi }, testInfo) => {
        const projectNameAttachment = testInfo.attachments.find(a => a.name === 'projectName');
        if (projectNameAttachment?.body) {
            const projectName = projectNameAttachment.body.toString();
            try {
                const projects = await projectsApi.getAll();
                const projectToDelete = projects.find(p => p.name === projectName);
                if (projectToDelete) {
                    await projectsApi.delete(projectToDelete.id);
                }
            } catch (error) {
                console.error(`Failed to delete project ${projectName}`);
            }
        }
    });

    test("create new project", async ({ page }, testInfo) => {
        const projectData = creatDefaultProjectDto();
        testInfo.attach('projectName', { body: projectData.name });

        await projectsPage.createProject(projectData);

        expect(await projectsPage.isProjectPresent(projectData.name)).toBe(true);

        const displayedName = await projectsPage.getProjectName(projectData.name);
        expect(displayedName).toBe(projectData.name);

        const displayedDescription = await projectsPage.getProjectDescription(projectData.name);
        expect(displayedDescription).toBe(projectData.description);

        await page.goto('/#/');
        const projectsCount = await dashboardPage.getProjectsCount();
        const originalProjectsCount = testInfo.attachments.find(a => a.name === 'projectsCount');
        const originalCount = parseInt(originalProjectsCount?.body?.toString() || '0');

        expect(projectsCount).toBe(originalCount + 1);
    });

    test("delete project", async ({ page }, testInfo) => {
        const projectData = creatDefaultProjectDto();

        await projectsPage.createProject(projectData);

        expect(await projectsPage.isProjectPresent(projectData.name)).toBe(true);

        await projectsPage.deleteProject(projectData.name);

        expect(await projectsPage.isProjectPresent(projectData.name)).toBe(false);

        await page.goto('/#/');
        const projectsCount = await dashboardPage.getProjectsCount();
        const originalProjectsCount = testInfo.attachments.find(a => a.name === 'projectsCount');
        const originalCount = parseInt(originalProjectsCount?.body?.toString() || '0');

        expect(projectsCount).toBe(originalCount);
    });

    test("edit project", async ({ page }, testInfo) => {
        const projectData = creatDefaultProjectDto();
        testInfo.attach('projectName', { body: projectData.name });

        await projectsPage.createProject(projectData);

        expect(await projectsPage.isProjectPresent(projectData.name)).toBe(true);

        const displayedName = await projectsPage.getProjectName(projectData.name);
        expect(displayedName).toBe(projectData.name);

        const displayedDescription = await projectsPage.getProjectDescription(projectData.name);
        expect(displayedDescription).toBe(projectData.description);

        const updatedName = `Updated_${projectData.name}`;
        const updatedDescription = "Updated project description";

        await projectsPage.editProject(projectData.name, {
            name: updatedName,
            description: updatedDescription
        });

        testInfo.attach('projectName', { body: updatedName });

        expect(await projectsPage.isProjectPresent(updatedName)).toBe(true);

        const newDisplayedName = await projectsPage.getProjectName(updatedName);
        expect(newDisplayedName).toBe(updatedName);

        const newDisplayedDescription = await projectsPage.getProjectDescription(updatedName);
        expect(newDisplayedDescription).toBe(updatedDescription);

        await page.goto('/#/');
        const projectsCount = await dashboardPage.getProjectsCount();
        const originalProjectsCount = testInfo.attachments.find(a => a.name === 'projectsCount');
        const originalCount = parseInt(originalProjectsCount?.body?.toString() || '0');

        expect(projectsCount).toBe(originalCount + 1);
    });

    test("create project with minimal data", async ({ page }, testInfo) => {
        const projectData = creatDefaultProjectDto({ description: undefined });
        testInfo.attach('projectName', { body: projectData.name });

        await projectsPage.createProject(projectData);

        expect(await projectsPage.isProjectPresent(projectData.name)).toBe(true);

        const displayedName = await projectsPage.getProjectName(projectData.name);
        expect(displayedName).toBe(projectData.name);

        await page.goto('/#/');
        const projectsCount = await dashboardPage.getProjectsCount();
        const originalProjectsCount = testInfo.attachments.find(a => a.name === 'projectsCount');
        const originalCount = parseInt(originalProjectsCount?.body?.toString() || '0');

        expect(projectsCount).toBe(originalCount + 1);
    });
});