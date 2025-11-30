<script lang="ts">
  import { onMount } from "svelte";
  import type { Task, TaskCreate, TaskUpdate, TaskStatus, Priority } from "../models/task";
  import type { User } from "../models/user";
  import type { Project } from "../models/project";
  import { getAllTasks, createTask, updateTask, deleteTask } from "../services/taskService";
  import { getAllUsers } from "../services/userService";
  import { getAllProjects } from "../services/projectService";
  import Modal from "../components/Modal.svelte";

  let loading = true;
  let tasks: Task[] = [];
  let users: User[] = [];
  let projects: Project[] = [];
  let error: string | null = null;

  // Create modal
  let isCreateModalOpen = false;
  let isCreating = false;
  let createError: string | null = null;
  let newTask: TaskCreate = {
    title: '',
    description: '',
    status: 'TODO',
    priority: 'MEDIUM',
    projectId: 0,
    dueDate: ''
  };

  // Edit modal
  let isEditModalOpen = false;
  let isEditing = false;
  let editError: string | null = null;
  let editingTask: Task | null = null;
  let editData: TaskUpdate = {};

  onMount(async () => {
    await Promise.all([loadTasks(), loadUsers(), loadProjects()]);
  });

  async function loadTasks() {
    try {
      loading = true;
      tasks = await getAllTasks();
      error = null;
    } catch (err) {
      error = err instanceof Error ? err.message : 'Failed to load tasks';
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

  async function loadProjects() {
    try {
      projects = await getAllProjects();
    } catch (err) {
      console.error('Failed to load projects:', err);
    }
  }

  function openCreateModal() {
    isCreateModalOpen = true;
    createError = null;
    newTask = {
      title: '',
      description: '',
      status: 'TODO',
      priority: 'MEDIUM',
      projectId: projects.length > 0 ? projects[0].id : 0,
      dueDate: ''
    };
  }

  function closeCreateModal() {
    isCreateModalOpen = false;
  }

  async function handleCreateTask() {
    try {
      isCreating = true;
      createError = null;
      await createTask(newTask);
      await loadTasks();
      closeCreateModal();
    } catch (err) {
      createError = err instanceof Error ? err.message : 'Failed to create task';
    } finally {
      isCreating = false;
    }
  }

  function openEditModal(task: Task) {
    editingTask = task;
    editData = {
      title: task.title,
      description: task.description,
      status: task.status,
      priority: task.priority,
      dueDate: task.dueDate || '',
      assigneeId: task.assignee?.id
    };
    isEditModalOpen = true;
    editError = null;
  }

  function closeEditModal() {
    isEditModalOpen = false;
    editingTask = null;
  }

  async function handleUpdateTask() {
    if (!editingTask) return;

    try {
      isEditing = true;
      editError = null;
      await updateTask(editingTask.id, editData);
      await loadTasks();
      closeEditModal();
    } catch (err) {
      editError = err instanceof Error ? err.message : 'Failed to update task';
    } finally {
      isEditing = false;
    }
  }

  async function handleDeleteTask(id: number, title: string) {
    if (!confirm(`Are you sure you want to delete task "${title}"?`)) {
      return;
    }

    try {
      await deleteTask(id);
      await loadTasks();
    } catch (err) {
      if (err instanceof Error && err.message.includes("Access denied")) {
        alert('⛔ ' + err.message)
      }
      alert('Failed to delete task: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  }

  function getPriorityColor(priority: string): string {
    switch (priority) {
      case 'HIGH': return 'bg-red-100 text-red-800 border-red-200';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'LOW': return 'bg-green-100 text-green-800 border-green-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  }

  function getStatusColor(status: string): string {
    switch (status) {
      case 'TODO': return 'bg-gray-100 text-gray-800';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800';
      case 'DONE': return 'bg-green-100 text-green-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  function formatDate(dateString: string | null): string {
    if (!dateString) return 'No deadline';
    const date = new Date(dateString);
    return date.toLocaleDateString('cs-CZ', { 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric' 
    });
  }
</script>

<div class="container mx-auto px-4 py-8">
  <div class="flex items-center justify-between mb-6">
    <h1 class="text-3xl font-bold text-gray-800">Tasks</h1>
    <button 
      on:click={openCreateModal}
      class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg font-medium transition flex items-center gap-2">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
      </svg>
      Create Task
    </button>
  </div>

  {#if loading}
    <div class="flex items-center justify-center py-12">
      <div class="text-gray-600">Loading tasks...</div>
    </div>
  {:else if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
      Error: {error}
    </div>
  {:else if tasks.length === 0}
    <div class="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded">
      No tasks found. Click "Create Task" to add one.
    </div>
  {:else}
    <div class="hidden md:block bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Task</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Project</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Assignee</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Priority</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Due Date</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          {#each tasks as task}
            <tr class="hover:bg-gray-50">
              <td class="px-6 py-4">
                <div class="text-sm font-medium text-gray-900">{task.title}</div>
                {#if task.description}
                  <div class="text-sm text-gray-500 truncate max-w-xs">{task.description}</div>
                {/if}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{task.projectName}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                {#if task.assignee}
                  <div class="flex items-center">
                    <div class="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white font-semibold text-sm">
                      {task.assignee.firstName?.[0]}{task.assignee.lastName?.[0]}
                    </div>
                    <div class="ml-3 text-sm font-medium text-gray-900">
                      {task.assignee.firstName} {task.assignee.lastName}
                    </div>
                  </div>
                {:else}
                  <span class="text-sm text-gray-400 italic">Unassigned</span>
                {/if}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full {getStatusColor(task.status)}">
                  {task.status.replace('_', ' ')}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full border {getPriorityColor(task.priority)}">
                  {task.priority}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDate(task.dueDate)}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                <button 
                  on:click={() => openEditModal(task)}
                  class="text-blue-600 hover:text-blue-900">
                  Edit
                </button>
                <button 
                  on:click={() => handleDeleteTask(task.id, task.title)}
                  class="text-red-600 hover:text-red-900">
                  Delete
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>

    <div class="md:hidden space-y-4">
      {#each tasks as task}
        <div class="bg-white rounded-lg shadow p-4">
          <div class="flex items-start justify-between mb-2">
            <h3 class="text-lg font-semibold text-gray-900">{task.title}</h3>
            <span class="px-2 py-1 text-xs font-semibold rounded-full {getStatusColor(task.status)}">
              {task.status.replace('_', ' ')}
            </span>
          </div>
          
          {#if task.description}
            <p class="text-sm text-gray-600 mb-3">{task.description}</p>
          {/if}

          <div class="space-y-2 text-sm mb-3">
            <div class="flex items-center text-gray-600">
              <span class="font-medium w-24">Project:</span>
              <span>{task.projectName}</span>
            </div>
            <div class="flex items-center text-gray-600">
              <span class="font-medium w-24">Assignee:</span>
              {#if task.assignee}
                <span>{task.assignee.firstName} {task.assignee.lastName}</span>
              {:else}
                <span class="italic text-gray-400">Unassigned</span>
              {/if}
            </div>
            <div class="flex items-center text-gray-600">
              <span class="font-medium w-24">Priority:</span>
              <span class="px-2 py-0.5 text-xs font-semibold rounded-full border {getPriorityColor(task.priority)}">
                {task.priority}
              </span>
            </div>
            <div class="flex items-center text-gray-600">
              <span class="font-medium w-24">Due Date:</span>
              <span>{formatDate(task.dueDate)}</span>
            </div>
          </div>

          <div class="flex gap-2 pt-3 border-t">
            <button 
              on:click={() => openEditModal(task)}
              class="flex-1 bg-blue-50 hover:bg-blue-100 text-blue-600 px-3 py-2 rounded transition text-sm font-medium">
              Edit
            </button>
            <button 
              on:click={() => handleDeleteTask(task.id, task.title)}
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
<Modal isOpen={isCreateModalOpen} title="Create New Task" onClose={closeCreateModal}>
  <form on:submit|preventDefault={handleCreateTask}>
    {#if createError}
      <div class="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {createError}
      </div>
    {/if}

    <div class="space-y-4">
      <div>
        <label for="title" class="block text-sm font-medium text-gray-700 mb-1">Title *</label>
        <input 
          type="text" 
          id="title"
          bind:value={newTask.title}
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
      </div>

      <div>
        <label for="description" class="block text-sm font-medium text-gray-700 mb-1">Description</label>
        <textarea 
          id="description"
          bind:value={newTask.description}
          rows="3"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"></textarea>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label for="status" class="block text-sm font-medium text-gray-700 mb-1">Status</label>
          <select 
            id="status"
            bind:value={newTask.status}
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value="TODO">TODO</option>
            <option value="IN_PROGRESS">IN PROGRESS</option>
            <option value="DONE">DONE</option>
          </select>
        </div>

        <div>
          <label for="priority" class="block text-sm font-medium text-gray-700 mb-1">Priority</label>
          <select 
            id="priority"
            bind:value={newTask.priority}
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
          </select>
        </div>
      </div>

      <div>
        <label for="project" class="block text-sm font-medium text-gray-700 mb-1">Project *</label>
        <select 
          id="project"
          bind:value={newTask.projectId}
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
          {#each projects as project}
            <option value={project.id}>{project.name}</option>
          {/each}
        </select>
      </div>

      <div>
        <label for="assignee" class="block text-sm font-medium text-gray-700 mb-1">Assignee</label>
        <select 
          id="assignee"
          bind:value={newTask.assigneeId}
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
          <option value={undefined}>Unassigned</option>
          {#each users as user}
            <option value={user.id}>
              {user.firstName} {user.lastName} ({user.username})
            </option>
          {/each}
        </select>
      </div>

      <div>
        <label for="dueDate" class="block text-sm font-medium text-gray-700 mb-1">Due Date</label>
        <input 
          type="date" 
          id="dueDate"
          bind:value={newTask.dueDate}
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
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
        {isCreating ? 'Creating...' : 'Create Task'}
      </button>
    </div>
  </form>
</Modal>

<!-- Edit Modal -->
<Modal isOpen={isEditModalOpen} title="Edit Task" onClose={closeEditModal}>
  <form on:submit|preventDefault={handleUpdateTask}>
    {#if editError}
      <div class="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {editError}
      </div>
    {/if}

    <div class="space-y-4">
      <div>
        <label for="edit-title" class="block text-sm font-medium text-gray-700 mb-1">Title</label>
        <input 
          type="text" 
          id="edit-title"
          bind:value={editData.title}
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
      </div>

      <div>
        <label for="edit-description" class="block text-sm font-medium text-gray-700 mb-1">Description</label>
        <textarea 
          id="edit-description"
          bind:value={editData.description}
          rows="3"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"></textarea>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label for="edit-status" class="block text-sm font-medium text-gray-700 mb-1">Status</label>
          <select 
            id="edit-status"
            bind:value={editData.status}
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value="TODO">TODO</option>
            <option value="IN_PROGRESS">IN PROGRESS</option>
            <option value="DONE">DONE</option>
          </select>
        </div>

        <div>
          <label for="edit-priority" class="block text-sm font-medium text-gray-700 mb-1">Priority</label>
          <select 
            id="edit-priority"
            bind:value={editData.priority}
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
          </select>
        </div>
      </div>

      <div>
        <label for="edit-assignee" class="block text-sm font-medium text-gray-700 mb-1">Assignee</label>
        <select 
          id="edit-assignee"
          bind:value={editData.assigneeId}
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
          <option value={null}>Unassigned</option>
          {#each users as user}
            <option value={user.id}>
              {user.firstName} {user.lastName} ({user.username})
            </option>
          {/each}
        </select>
      </div>

      <div>
        <label for="edit-dueDate" class="block text-sm font-medium text-gray-700 mb-1">Due Date</label>
        <input 
          type="date" 
          id="edit-dueDate"
          bind:value={editData.dueDate}
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
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