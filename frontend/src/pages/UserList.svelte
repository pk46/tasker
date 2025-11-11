<script lang="ts">
    import { onMount } from "svelte";
    import type { User } from "../models/user";
    import { getAllUsers } from "../services/userService";

    let users: User[] = [];
    let loading = true;
    let error: string | null = null;

    onMount(async () => {
        try {
            users = await getAllUsers();
            loading = false;
        } catch(err) {
            error = err instanceof Error ? err.message : "Failed to load users";
            loading = false;
        }
    });
</script>

<div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">Users</h1>

    {#if loading}
    <div class="flex items-center justify-center py-12">
        <div class="text-gray-600">Loading users...</div>
    </div>
    {:else if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-7 rounded">
        Error: {error}
    </div>
    {:else if users.length === 0}
    <div class="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded">
      No users found.
    </div>
  {:else}
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Username</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Role</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
            {#each users as user}
                <tr class="hover:bg-gray-50">
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{user.id}</td>
                     <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{user.username}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{user.email}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {user.firstName} {user.lastName}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full
                  {user.role === 'ADMIN' ? 'bg-purple-100 text-purple-800' : 'bg-green-100 text-green-800'}">
                  {user.role}
                </span>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
    {/if}
</div>