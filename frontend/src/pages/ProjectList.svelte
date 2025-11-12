<script lang="ts">
  import { onMount } from 'svelte';
  import type { Project, ProjectCreate, ProjectUpdate } from '../models/project';
  import type { User } from '../models/user';
  import { getAllProjects, createProject, updateProject, deleteProject } from '../services/projectService';
  import { getAllUsers } from '../services/userService';
  import Modal from '../components/Modal.svelte';

  let projects: Project[] = [];
  let users: User[] = [];
  let loading = true;
  let error: string | null = null;

  // Create modal
  let isCreateModalOpen = false;
  let isCreating = false;
  let createError: string | null = null;
  let newProject: ProjectCreate = { name: '', description: '' };
  let selectedOwnerId: number | null = null;

  // Edit modal
  let isEditModalOpen = false;
  let isEditing = false;
  let editError: string | null = null;
  let editingProject: Project | null = null;
  let editData: ProjectUpdate = {};

  onMount(async () => {
    await Promise.all([loadProjects(), loadUsers()]);
  });

  async function loadProjects() {
    try {
      loading = true;
      projects = await getAllProjects();
      error = null;
    } catch (err) {
      error = err instanceof Error ? err.message : 'Failed to load projects';
    } finally {
      loading = false;
    }
  }

  async function loadUsers() {
    try {
      users = await getAllUsers();
    } catch (err) {
      console.error('Failed to load users:', err);
    }
  }

  function openCreateModal() {
    isCreateModalOpen = true;
    createError = null;
    newProject = { name: '', description: '' };
    selectedOwnerId = users.length > 0 ? users[0].id : null;
  }

  function closeCreateModal() {
    isCreateModalOpen = false;
  }

  async function handleCreateProject() {
    if (!selectedOwnerId) {
      createError = 'Please select an owner';
      return;
    }

    try {
      isCreating = true;
      createError = null;
      await createProject(newProject, selectedOwnerId);
      await loadProjects();
      closeCreateModal();
    } catch (err) {
      createError = err instanceof Error ? err.message : 'Failed to create project';
    } finally {
      isCreating = false;
    }
  }

  function openEditModal(project: Project) {
    editingProject = project;
    editData = {
      name: project.name,
      description: project.description
    };
    isEditModalOpen = true;
    editError = null;
  }

  function closeEditModal() {
    isEditModalOpen = false;
    editingProject = null;
  }

  async function handleUpdateProject() {
    if (!editingProject) return;

    try {
      isEditing = true;
      editError = null;
      await updateProject(editingProject.id, editData);
      await loadProjects();
      closeEditModal();
    } catch (err) {
      editError = err instanceof Error ? err.message : 'Failed to update project';
    } finally {
      isEditing = false;
    }
  }

  async function handleDeleteProject(id: number, name: string) {
    if (!confirm(`Are you sure you want to delete project "${name}"?`)) {
      return;
    }

    try {
      await deleteProject(id);
      await loadProjects();
    } catch (err) {
      alert('Failed to delete project: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  }

  function formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('cs-CZ', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
</script>

<div class="container mx-auto px-4 py-8">
  <div class="flex items-center justify-between mb-6">
    <h1 class="text-3xl font-bold text-gray-800">Projects</h1>
    <button 
      on:click={openCreateModal}
      class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg font-medium transition flex items-center gap-2">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
      </svg>
      Create Project
    </button>
  </div>

  {#if loading}
    <div class="flex items-center justify-center py-12">
      <div class="text-gray-600">Loading projects...</div>
    </div>
  {:else if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
      Error: {error}
    </div>
  {:else if projects.length === 0}
    <div class="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded">
      No projects found. Click "Create Project" to add one.
    </div>
  {:else}
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {#each projects as project}
        <div class="bg-white rounded-lg shadow hover:shadow-lg transition p-6">
          <div class="flex items-start justify-between mb-3">
            <h3 class="text-xl font-semibold text-gray-800">{project.name}</h3>
          </div>
          
          {#if project.description}
            <p class="text-gray-600 mb-4 line-clamp-3">{project.description}</p>
          {:else}
            <p class="text-gray-400 italic mb-4">No description</p>
          {/if}

          <div class="border-t pt-4 space-y-2 text-sm">
            <div class="flex items-center text-gray-600">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              <span class="font-medium">Owner:</span>
              <span class="ml-1">{project.owner.firstName} {project.owner.lastName}</span>
            </div>
            <div class="flex items-center text-gray-600">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span class="font-medium">Created:</span>
              <span class="ml-1">{formatDate(project.createdAt)}</span>
            </div>
            <div class="flex items-center text-gray-600">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              <span class="font-medium">Tasks:</span>
              <span class="ml-1">{project.taskCount}</span>
            </div>
          </div>

          <div class="mt-4 pt-4 border-t flex gap-2">
            <button 
              on:click={() => openEditModal(project)}
              class="flex-1 bg-blue-50 hover:bg-blue-100 text-blue-600 px-3 py-2 rounded transition text-sm font-medium">
              Edit
            </button>
            <button 
              on:click={() => handleDeleteProject(project.id, project.name)}
              class="flex-1 bg-red-50 hover:bg-red-100 text-red-600 px-3 py-2 rounded transition text-sm font-medium">
              Delete
            </button>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<!-- Create Modal -->
<Modal isOpen={isCreateModalOpen} title="Create New Project" onClose={closeCreateModal}>
  <form on:submit|preventDefault={handleCreateProject}>
    {#if createError}
      <div class="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {createError}
      </div>
    {/if}

    <div class="space-y-4">
      <div>
        <label for="name" class="block text-sm font-medium text-gray-700 mb-1">
          Project Name *
        </label>
        <input 
          type="text" 
          id="name"
          bind:value={newProject.name}
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Website Redesign" />
      </div>

      <div>
        <label for="description" class="block text-sm font-medium text-gray-700 mb-1">
          Description
        </label>
        <textarea 
          id="description"
          bind:value={newProject.description}
          rows="4"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Project description..."></textarea>
      </div>

      <div>
        <label for="owner" class="block text-sm font-medium text-gray-700 mb-1">
          Owner *
        </label>
        <select 
          id="owner"
          bind:value={selectedOwnerId}
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
          {#each users as user}
            <option value={user.id}>
              {user.firstName} {user.lastName} ({user.username})
            </option>
          {/each}
        </select>
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
        {isCreating ? 'Creating...' : 'Create Project'}
      </button>
    </div>
  </form>
</Modal>

<!-- Edit Modal -->
<Modal isOpen={isEditModalOpen} title="Edit Project" onClose={closeEditModal}>
  <form on:submit|preventDefault={handleUpdateProject}>
    {#if editError}
      <div class="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {editError}
      </div>
    {/if}

    <div class="space-y-4">
      <div>
        <label for="edit-name" class="block text-sm font-medium text-gray-700 mb-1">
          Project Name
        </label>
        <input 
          type="text" 
          id="edit-name"
          bind:value={editData.name}
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
      </div>

      <div>
        <label for="edit-description" class="block text-sm font-medium text-gray-700 mb-1">
          Description
        </label>
        <textarea 
          id="edit-description"
          bind:value={editData.description}
          rows="4"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"></textarea>
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