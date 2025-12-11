import { test as base } from '@playwright/test';
import { ApiClient } from '../api/ApiClient';
import { AuthApi } from '../api/AuthApi';
import { TaskApi } from '../api/TaskApi';
import { UsersApi } from '../api/UsersApi';
import { ProjectsApi } from '../api/ProjectsApi';

/**
 * Extended test fixtures with authenticated API clients
 *
 * Usage:
 * import { test } from '../lib/fixtures/authenticated';
 *
 * test('create task', async ({ tasksApi }) => {
 *   const task = await tasksApi.create({ title: 'Test', projectId: 1 });
 *   // ... test logic
 *   await tasksApi.delete(task.id);
 * });
 */
type AuthenticatedFixtures = {
    apiClient: ApiClient;
    authApi: AuthApi;
    tasksApi: TaskApi;
    usersApi: UsersApi;
    projectsApi: ProjectsApi;
};

export const test = base.extend<AuthenticatedFixtures>({

    apiClient: async ({ request }, use) => {
        const client = new ApiClient(request);
        const authApi = new AuthApi(client);

        const username = process.env.ADMIN_USERNAME || 'admin';
        const password = process.env.ADMIN_PASSWORD || 'admin';

        try {
            await authApi.login(username, password);
            console.info(`Authenticated as ${username}`);
        } catch (error) {
            console.error('Authentication failed:', error);
            throw new Error(
                `Failed to authenticate with credentials: ${username}/${password}\n` +
                `Make sure the backend is running and credentials are correct.`
            );
        }

        await use(client);
    },

    authApi: async ({ apiClient }, use) => {
        await use(new AuthApi(apiClient));
    },

    tasksApi: async ({ apiClient }, use) => {
        await use(new TaskApi(apiClient));
    },

    usersApi: async ({ apiClient }, use) => {
        await use(new UsersApi(apiClient));
    },

    projectsApi: async ({ apiClient }, use) => {
        await use(new ProjectsApi(apiClient));
    },
});

export { expect } from '@playwright/test';
