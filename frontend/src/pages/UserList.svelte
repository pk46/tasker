<script lang="ts">
  import { onMount } from 'svelte';
  import type { User, UserCreate } from '../models/user';
  import { getAllUsers, createUser, deleteUser } from '../services/userService';
  import Modal from '../components/Modal.svelte';

  let users: User[] = [];
  let loading = true;
  let error: string | null = null;

  // Modal state
  let isCreateModalOpen = false;
  let isCreating = false;
  let createError: string | null = null;

  // Form data
  let newUser: UserCreate = {
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: ''
  };

  onMount(async () => {
    await loadUsers();
  });

  async function loadUsers() {
    try {
      loading = true;
      users = await getAllUsers();
      error = null;
    } catch (err) {
      error = err instanceof Error ? err.message : 'Failed to load users';
    } finally {
      loading = false;
    }
  }

  function openCreateModal() {
    isCreateModalOpen = true;
    createError = null;
    // Reset form
    newUser = {
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: ''
    };
  }

  function closeCreateModal() {
    isCreateModalOpen = false;
  }

  async function handleCreateUser() {
    try {
      isCreating = true;
      createError = null;
      
      await createUser(newUser);
      
      // Reload users
      await loadUsers();
      
      // Close modal
      closeCreateModal();
    } catch (err) {
      createError = err instanceof Error ? err.message : 'Failed to create user';
    } finally {
      isCreating = false;
    }
  }

  async function handleDeleteUser(id: number, username: string) {
    if (!confirm(`Are you sure you want to delete user "${username}"?`)) {
      return;
    }

    try {
      await deleteUser(id);
      await loadUsers();
    } catch (err) {
      alert('Failed to delete user: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  }
</script>

<div class="container mx-auto px-4 py-8">
  <!-- Header with Create button -->
  <div class="flex items-center justify-between mb-6">
    <h1 class="text-3xl font-bold text-gray-800">Users</h1>
    <button 
      on:click={openCreateModal}
      class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg font-medium transition flex items-center gap-2">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
      </svg>
      Create User
    </button>
  </div>

  {#if loading}
    <div class="flex items-center justify-center py-12">
      <div class="text-gray-600">Loading users...</div>
    </div>
  {:else if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
      Error: {error}
    </div>
  {:else if users.length === 0}
    <div class="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded">
      No users found. Click "Create User" to add one.
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
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
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
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button 
                  on:click={() => handleDeleteUser(user.id, user.username)}
                  class="text-red-600 hover:text-red-900 transition">
                  Delete
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}
</div>

<!-- Create User Modal -->
<Modal 
  isOpen={isCreateModalOpen} 
  title="Create New User" 
  onClose={closeCreateModal}>
  
  <form on:submit|preventDefault={handleCreateUser}>
    {#if createError}
      <div class="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {createError}
      </div>
    {/if}

    <div class="space-y-4">
      <!-- Username -->
      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 mb-1">
          Username *
        </label>
        <input 
          type="text" 
          id="username"
          bind:value={newUser.username}
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="john_doe" />
      </div>

      <!-- Email -->
      <div>
        <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
          Email *
        </label>
        <input 
          type="email" 
          id="email"
          bind:value={newUser.email}
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="john@example.com" />
      </div>

      <!-- Password -->
      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
          Password *
        </label>
        <input 
          type="password" 
          id="password"
          bind:value={newUser.password}
          required
          minlength="6"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Min. 6 characters" />
      </div>

      <!-- First Name -->
      <div>
        <label for="firstName" class="block text-sm font-medium text-gray-700 mb-1">
          First Name
        </label>
        <input 
          type="text" 
          id="firstName"
          bind:value={newUser.firstName}
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="John" />
      </div>

      <!-- Last Name -->
      <div>
        <label for="lastName" class="block text-sm font-medium text-gray-700 mb-1">
          Last Name
        </label>
        <input 
          type="text" 
          id="lastName"
          bind:value={newUser.lastName}
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Doe" />
      </div>
    </div>

    <!-- Modal Actions -->
    <div class="mt-6 flex justify-end gap-3">
      <button 
        type="button"
        on:click={closeCreateModal}
        disabled={isCreating}
        class="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition disabled:opacity-50">
        Cancel
      </button>
      <button 
        type="submit"
        disabled={isCreating}
        class="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg transition disabled:opacity-50">
        {isCreating ? 'Creating...' : 'Create User'}
      </button>
    </div>
  </form>
</Modal>