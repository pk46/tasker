<script lang="ts">
  import { onMount } from 'svelte';
  import type { User, UserCreate, UserUpdate } from '../models/user';
  import { getAllUsers, createUser, updateUser, deleteUser } from '../services/userService';
  import Modal from '../components/Modal.svelte';
  import { authStore } from '../stores/authStore';

  let users: User[] = [];
  let adminCount: number = 0;
  let loading = true;
  let error: string | null = null;
  let currentUser: User | null = null;

  // Create modal 
  let isCreateModalOpen = false;
  let isCreating = false;
  let createError: string | null = null;
  let newUser: UserCreate = {
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    role: '',
  };

  // Edit modal
  let isEditModalOpen = false;
  let isEditing = false;
  let editError: string | null = null;
  let editingUser: User | null = null;
  let editData: UserUpdate = {};

  authStore.subscribe(state => {
    currentUser = state.user;
  });

  
  onMount(async () => {
    await loadUsers();
  });

  async function loadUsers() {
    try {
      loading = true;
      users = await getAllUsers();
      await getAdminCounts();
      error = null;
    } catch (err) {
      error = err instanceof Error ? err.message : 'Failed to load users';
    } finally {
      loading = false;
    }
  }

  async function getAdminCounts() {
    const admins = users.filter((user) => user.role === "ADMIN")
    adminCount = admins.length;
    return adminCount;
  }

  function openCreateModal() {
    isCreateModalOpen = true;
    createError = null;
    newUser = {
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      role: '',
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
      await loadUsers();
      closeCreateModal();
    } catch (err) {
      createError = err instanceof Error ? err.message : 'Failed to create user';
    } finally {
      isCreating = false;
    }
  }

  function openEditModal(user: User) {
    editingUser = user;
    editData = {
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      password: '',
      role: user.role
    };
    isEditModalOpen = true;
    editError = null;
  }

  function closeEditModal() {
    isEditModalOpen = false;
    editingUser = null;
  }

  async function handleUpdateUser() {
    if (!editingUser) return;

    try {
      isEditing = true;
      editError = null;
      
      const updatePayload = { ...editData };
      if (!updatePayload.password || updatePayload.password.trim() === '') {
        delete updatePayload.password;
      }
      
      await updateUser(editingUser.id, updatePayload);
      await loadUsers();
      closeEditModal();
    } catch (err) {
      editError = err instanceof Error ? err.message : 'Failed to update user';
    } finally {
      isEditing = false;
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
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                <button 
                  on:click={() => openEditModal(user)}
                  class="text-blue-600 hover:text-blue-900 transition">
                  Edit
                </button>
                {#if !(adminCount === 1 && user.role === "ADMIN")}
                <button
                  on:click={() => handleDeleteUser(user.id, user.username)}
                  class="text-red-600 hover:text-red-900 transition">
                  Delete
                </button>
                {/if}
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

      <div class="grid grid-cols-2 gap-4">
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

        <div>
          <label for="role" class="block text-sm font-medium text-gray-700 mb-1">
            Role
          </label>
          <select
            id="role"
            bind:value={newUser.role}
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value="">Select role</option>
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>
      </div>
    </div>

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

<!-- Edit User Modal -->
<Modal 
  isOpen={isEditModalOpen} 
  title="Edit User" 
  onClose={closeEditModal}>
  
  <form on:submit|preventDefault={handleUpdateUser}>
    {#if editError}
      <div class="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {editError}
      </div>
    {/if}

    <div class="space-y-4">
      <div>
        <label for="edit-email" class="block text-sm font-medium text-gray-700 mb-1">
          Email
        </label>
        <input 
          type="email" 
          id="edit-email"
          bind:value={editData.email}
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
      </div>

      <div>
        <label for="edit-password" class="block text-sm font-medium text-gray-700 mb-1">
          New Password (leave empty to keep current)
        </label>
        <input 
          type="password" 
          id="edit-password"
          bind:value={editData.password}
          minlength="6"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Min. 6 characters if changing" />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label for="edit-firstName" class="block text-sm font-medium text-gray-700 mb-1">
            First Name
          </label>
          <input 
            type="text" 
            id="edit-firstName"
            bind:value={editData.firstName}
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
        </div>

        <div>
          <label for="edit-lastName" class="block text-sm font-medium text-gray-700 mb-1">
            Last Name
          </label>
          <input 
            type="text" 
            id="edit-lastName"
            bind:value={editData.lastName}
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
        </div>
        <div>
        <label for="edit-role" class="block text-sm font-medium text-gray-700 mb-1">
            Role
          </label>
          <select
            id="edit-role"
            bind:value={editData.role}
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>
      </div>
    </div>

    <div class="mt-6 flex justify-end gap-3">
      <button 
        type="button"
        on:click={closeEditModal}
        disabled={isEditing}
        class="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition disabled:opacity-50">
        Cancel
      </button>
      <button 
        type="submit"
        disabled={isEditing}
        class="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg transition disabled:opacity-50">
        {isEditing ? 'Saving...' : 'Save Changes'}
      </button>
    </div>
  </form>
</Modal>