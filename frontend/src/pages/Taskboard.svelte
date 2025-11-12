<script lang="ts">
  import { onMount } from 'svelte';
  import type { Task, TaskCreate, TaskStatus, TaskUpdate } from '../models/task';
  import type { Project } from '../models/project';
  import { createTask, getAllTasks, updateTask, deleteTask } from '../services/taskService';
  import { getAllProjects } from '../services/projectService';
  import { getAllUsers } from '../services/userService';
  import type { User } from '../models/user';
  import Modal from '../components/Modal.svelte';

  let tasks: Task[] = [];
  let projects: Project[] = [];
  let users: User[] = [];
  let loading = true;
  let error: string | null = null;

  let selectedProjectId: number | null = null;
  let filteredTasks: Task[] = [];

  let todoTasks: Task[] = [];
  let inProgressTasks: Task[] = [];
  let doneTasks: Task[] = [];

  let isEditModalOpen = false;
  let isEditing = false;
  let editError: string | null = null;
  let editingTask: Task | null = null;
  let editData: TaskUpdate = {}

  // Drag & Drop state
  let draggedTask: Task | null = null;

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

  onMount(async () => {
    await Promise.all([loadTasks(), loadProjects(), loadUsers()]);
  });

  async function loadTasks() {
    try {
      loading = true;
      tasks = await getAllTasks();
      filterAndOrganizeTasks();
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

  function filterAndOrganizeTasks() {
    if (selectedProjectId) {
      filteredTasks = tasks.filter(t => t.projectId === selectedProjectId);
    } else {
      filteredTasks = tasks;
    }

    todoTasks = filteredTasks.filter(t => t.status === 'TODO');
    inProgressTasks = filteredTasks.filter(t => t.status === 'IN_PROGRESS');
    doneTasks = filteredTasks.filter(t => t.status === 'DONE');
  }

  function handleProjectChange() {
    filterAndOrganizeTasks();
  }

  function openCreateModal() {
  if (!selectedProjectId) return;
  
  isCreateModalOpen = true;
  createError = null;
  newTask = {
    title: '',
    description: '',
    status: 'TODO',
    priority: 'MEDIUM',
    projectId: selectedProjectId,
    dueDate: ''
  };
}

function closeCreateModal() {
  isCreateModalOpen = false;
}

function closeEditModal() {
    isEditModalOpen = false;
}

async function handleCreateTask() {
  try {
    isCreating = true;
    createError = null;
    await createTask(newTask);
    await Promise.all([loadTasks(), loadProjects()]);
    closeCreateModal();
  } catch (err) {
    createError = err instanceof Error ? err.message : 'Failed to create task';
  } finally {
    isCreating = false;
  }
}

async function handleUpdateTask() {
  if (!editingTask) return;

  try {
    isEditing = true;
    editError = null;
    await updateTask(editingTask.id, editData);
    await Promise.all([loadTasks(), loadProjects()]);
    closeEditModal();
  } catch (err) {
    editError = err instanceof Error ? err.message : 'Failed to update task';
  } finally {
    isEditing = false;
  }
}

async function handleDeleteTask(taskId: number, taskTitle: string) {
  if (!confirm(`Are you sure you want to delete task "${taskTitle}"?`)) {
    return;
  }

  try {
    await deleteTask(taskId);
    await Promise.all([loadTasks(), loadProjects()]);
    closeEditModal();
  } catch (err) {
    alert('Failed to delete task: ' + (err instanceof Error ? err.message : 'Unknown error'));
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
    assigneeId: task.assignee?.id || undefined
  };
  isEditModalOpen = true;
  editError = null;
}

  // Drag handlers
  function handleDragStart(event: DragEvent, task: Task) {
    event.stopPropagation()
    draggedTask = task;
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.dropEffect = 'move';
    }
  }

  function handleDragOver(event: DragEvent) {
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
  }

  async function handleDrop(event: DragEvent, newStatus: TaskStatus) {
    event.preventDefault();
    
    if (!draggedTask || draggedTask.status === newStatus) {
      draggedTask = null;
      return;
    }

    try {
      await updateTask(draggedTask.id, {
      title: draggedTask.title,
      description: draggedTask.description,
      status: newStatus,
      priority: draggedTask.priority,
      dueDate: draggedTask.dueDate || undefined,
      assigneeId: draggedTask.assignee?.id || undefined
    });
      await Promise.all([loadTasks(), loadProjects()]);
    } catch (err) {
      alert('Failed to update task status: ' + (err instanceof Error ? err.message : 'Unknown error'));
    } finally {
      draggedTask = null;
    }
  }

  function getPriorityColor(priority: string): string {
    switch (priority) {
      case 'HIGH': return 'border-l-4 border-l-red-500';
      case 'MEDIUM': return 'border-l-4 border-l-yellow-500';
      case 'LOW': return 'border-l-4 border-l-green-500';
      default: return 'border-l-4 border-l-gray-500';
    }
  }

  function formatDate(dateString: string | null): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('cs-CZ', { day: '2-digit', month: '2-digit' });
  }
</script>

<div class="container mx-auto px-4 py-8 max-w-7xl">
  <div class="mb-6">
    <div class="flex items-center justify-between mb-4">
      <div>
        <h1 class="text-3xl font-bold text-gray-800">Kanban Board</h1>
        <p class="text-gray-600 mt-1">Drag tasks between columns to change their status</p>
      </div>
    </div>

    <!-- Project Filter + Create Button -->
    <div class="bg-white rounded-lg shadow p-4">
      <div class="flex flex-col sm:flex-row sm:items-end gap-4">
        <div class="flex-1">
          <label for="project-filter" class="block text-sm font-medium text-gray-700 mb-2">
            Filter by Project
          </label>
          <select 
            id="project-filter"
            bind:value={selectedProjectId}
            on:change={handleProjectChange}
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value={null}>All Projects</option>
            {#each projects as project}
              <option value={project.id}>
                {project.name} ({project.taskCount} tasks)
              </option>
            {/each}
          </select>
        </div>
        
        <button 
          on:click={openCreateModal}
          disabled={!selectedProjectId}
          class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg font-medium transition flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Create Task
        </button>
      </div>
      
      {#if !selectedProjectId}
        <p class="text-xs text-gray-500 mt-2 italic">
          Select a project to create a new task
        </p>
      {/if}
    </div>
  </div>

  {#if loading}
    <div class="flex items-center justify-center py-12">
      <div class="text-gray-600">Loading tasks...</div>
    </div>
  {:else if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
      Error: {error}
    </div>
  {:else}
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      
      <!-- TODO Column -->
      <div 
        class="bg-gray-50 rounded-lg p-3 min-h-[500px]"
        on:dragover={handleDragOver}
        on:drop={(e) => handleDrop(e, 'TODO')}
        role="region"
        aria-label="TODO tasks">
        
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-base font-semibold text-gray-700 flex items-center gap-2">
            <div class="w-2.5 h-2.5 rounded-full bg-gray-400"></div>
            TODO
          </h2>
          <span class="text-xs text-gray-500 bg-white px-2 py-0.5 rounded">
            {todoTasks.length}
          </span>
        </div>

        <div class="space-y-2">
          {#each todoTasks as task}
            <div
              draggable="true"
              on:dragstart={(e) => handleDragStart(e, task)}
              on:click={() => openEditModal(task)}
              class="bg-white rounded-lg shadow p-3 cursor-move hover:shadow-md transition {getPriorityColor(task.priority)}"
              role="button"
              tabindex="0">
              
              <h3 class="font-medium text-sm text-gray-900 mb-1">{task.title}</h3>
              
              {#if task.description}
                <p class="text-xs text-gray-600 mb-2 line-clamp-2">{task.description}</p>
              {/if}

              <div class="flex items-center justify-between text-xs text-gray-500 mb-2">
                <span class="bg-gray-100 px-2 py-0.5 rounded truncate max-w-[120px]" title={task.projectName}>
                  {task.projectName}
                </span>
                {#if task.dueDate}
                  <span class="flex items-center gap-1">
                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {formatDate(task.dueDate)}
                  </span>
                {/if}
              </div>

              {#if task.assignee}
                <div class="flex items-center gap-1.5">
                  <div class="w-5 h-5 rounded-full bg-blue-500 flex items-center justify-center text-white text-xs font-semibold">
                    {task.assignee.firstName?.[0]}{task.assignee.lastName?.[0]}
                  </div>
                  <span class="text-xs text-gray-600 truncate">
                    {task.assignee.firstName} {task.assignee.lastName}
                  </span>
                </div>
              {/if}
            </div>
          {/each}

          {#if todoTasks.length === 0}
            <div class="text-center text-gray-400 italic text-sm py-8">
              No tasks
            </div>
          {/if}
        </div>
      </div>

      <!-- IN PROGRESS Column -->
      <div 
        class="bg-blue-50 rounded-lg p-3 min-h-[500px]"
        on:dragover={handleDragOver}
        on:drop={(e) => handleDrop(e, 'IN_PROGRESS')}
        role="region"
        aria-label="In progress tasks">
        
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-base font-semibold text-blue-700 flex items-center gap-2">
            <div class="w-2.5 h-2.5 rounded-full bg-blue-500"></div>
            IN PROGRESS
          </h2>
          <span class="text-xs text-blue-600 bg-white px-2 py-0.5 rounded">
            {inProgressTasks.length}
          </span>
        </div>

        <div class="space-y-2">
          {#each inProgressTasks as task}
            <div
              draggable="true"
              on:dragstart={(e) => handleDragStart(e, task)}
              on:click={() => openEditModal(task)}
              class="bg-white rounded-lg shadow p-3 cursor-move hover:shadow-md transition {getPriorityColor(task.priority)}"
              role="button"
              tabindex="0">
              
              <h3 class="font-medium text-sm text-gray-900 mb-1">{task.title}</h3>
              
              {#if task.description}
                <p class="text-xs text-gray-600 mb-2 line-clamp-2">{task.description}</p>
              {/if}

              <div class="flex items-center justify-between text-xs text-gray-500 mb-2">
                <span class="bg-gray-100 px-2 py-0.5 rounded truncate max-w-[120px]" title={task.projectName}>
                  {task.projectName}
                </span>
                {#if task.dueDate}
                  <span class="flex items-center gap-1">
                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {formatDate(task.dueDate)}
                  </span>
                {/if}
              </div>

              {#if task.assignee}
                <div class="flex items-center gap-1.5">
                  <div class="w-5 h-5 rounded-full bg-blue-500 flex items-center justify-center text-white text-xs font-semibold">
                    {task.assignee.firstName?.[0]}{task.assignee.lastName?.[0]}
                  </div>
                  <span class="text-xs text-gray-600 truncate">
                    {task.assignee.firstName} {task.assignee.lastName}
                  </span>
                </div>
              {/if}
            </div>
          {/each}

          {#if inProgressTasks.length === 0}
            <div class="text-center text-gray-400 italic text-sm py-8">
              No tasks
            </div>
          {/if}
        </div>
      </div>

      <!-- DONE Column -->
      <div 
        class="bg-green-50 rounded-lg p-3 min-h-[500px]"
        on:dragover={handleDragOver}
        on:drop={(e) => handleDrop(e, 'DONE')}
        role="region"
        aria-label="Done tasks">
        
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-base font-semibold text-green-700 flex items-center gap-2">
            <div class="w-2.5 h-2.5 rounded-full bg-green-500"></div>
            DONE
          </h2>
          <span class="text-xs text-green-600 bg-white px-2 py-0.5 rounded">
            {doneTasks.length}
          </span>
        </div>

        <div class="space-y-2">
          {#each doneTasks as task}
            <div
              draggable="true"
              on:dragstart={(e) => handleDragStart(e, task)}
              on:click={() => openEditModal(task)}
              class="bg-white rounded-lg shadow p-3 cursor-move hover:shadow-md transition {getPriorityColor(task.priority)} opacity-75"
              role="button"
              tabindex="0">
              
              <h3 class="font-medium text-sm text-gray-900 mb-1 line-through">{task.title}</h3>
              
              {#if task.description}
                <p class="text-xs text-gray-600 mb-2 line-clamp-2">{task.description}</p>
              {/if}

              <div class="flex items-center justify-between text-xs text-gray-500 mb-2">
                <span class="bg-gray-100 px-2 py-0.5 rounded truncate max-w-[120px]" title={task.projectName}>
                  {task.projectName}
                </span>
                {#if task.dueDate}
                  <span class="flex items-center gap-1">
                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {formatDate(task.dueDate)}
                  </span>
                {/if}
              </div>

              {#if task.assignee}
                <div class="flex items-center gap-1.5">
                  <div class="w-5 h-5 rounded-full bg-green-500 flex items-center justify-center text-white text-xs font-semibold">
                    {task.assignee.firstName?.[0]}{task.assignee.lastName?.[0]}
                  </div>
                  <span class="text-xs text-gray-600 truncate">
                    {task.assignee.firstName} {task.assignee.lastName}
                  </span>
                </div>
              {/if}
            </div>
          {/each}

          {#if doneTasks.length === 0}
            <div class="text-center text-gray-400 italic text-sm py-8">
              No tasks
            </div>
          {/if}
        </div>
      </div>

    </div>
  {/if}
</div>

<!-- Create Task Modal -->
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
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Task title..." />
      </div>

      <div>
        <label for="description" class="block text-sm font-medium text-gray-700 mb-1">Description</label>
        <textarea 
          id="description"
          bind:value={newTask.description}
          rows="3"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="Task description..."></textarea>
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
          disabled
          class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 cursor-not-allowed">
          {#each projects as project}
            <option value={project.id}>
              {project.name}
            </option>
          {/each}
        </select>
        <p class="text-xs text-gray-500 mt-1">Project is pre-selected from filter</p>
      </div>

      <div>
        <label for="assignee" class="block text-sm font-medium text-gray-700 mb-1">Assignee</label>
        <select 
          id="assignee"
          bind:value={newTask.assigneeId}
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
<!-- Edit Task Modal - ← PŘIDEJ tento celý modal -->
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

      <div class="bg-gray-50 p-3 rounded-lg">
        <p class="text-sm text-gray-600">
          <span class="font-medium">Project:</span> {editingTask?.projectName}
        </p>
        <p class="text-xs text-gray-500 mt-1">Project cannot be changed after creation</p>
      </div>
    </div>

    <div class="mt-6 flex justify-between">
      <button 
        type="button"
        on:click={() => handleDeleteTask(editingTask!.id, editingTask!.title)}
        disabled={isEditing}
        class="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition disabled:opacity-50">
        Delete Task
      </button>
      
      <div class="flex gap-3">
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
    </div>
  </form>
</Modal>