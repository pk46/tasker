import { test as base } from '@playwright/test';

type UserHelperFixtures = {
    uniqueEmail: string;
    uniqueUsername: string;
};

export const test = base.extend<UserHelperFixtures>({
    uniqueEmail: async ({}, use) => {
        const email = `auto${Date.now()}@test.cz`;
        await use(email);
    },

    uniqueUsername: async ({}, use) => {
        const username = `user${Date.now()}`;
        await use(username);
    }
});