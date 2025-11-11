<script lang="ts">
  import { onMount } from 'svelte';
  import { getAllUsers } from '../services/userService';
  import { getAllProjects } from '../services/projectService';
  import { getAllTasks } from '../services/taskService';

  let stats = [
    { label: 'Total Users', value: 0, color: 'bg-blue-500', icon: '👥' },
    { label: 'Total Projects', value: 0, color: 'bg-green-500', icon: '📁' },
    { label: 'Total Tasks', value: 0, color: 'bg-yellow-500', icon: '📝' },
    { label: 'Completed Tasks', value: 0, color: 'bg-purple-500', icon: '✅' },
  ];

  let loading = true;
  let error: string | null = null;

  onMount(async () => {
    await loadStats();
  });

  async function loadStats() {
    try {
      loading = true;
      error = null;

      const [users, projects, tasks] = await Promise.all([
        getAllUsers(),
        getAllProjects(),
        getAllTasks()
      ]);

      const completedTasks = tasks.filter(task => task.status === 'DONE').length;

      stats = [
        { label: 'Total Users', value: users.length, color: 'bg-blue-500', icon: '👥' },
        { label: 'Total Projects', value: projects.length, color: 'bg-green-500', icon: '📁' },
        { label: 'Total Tasks', value: tasks.length, color: 'bg-yellow-500', icon: '📝' },
        { label: 'Completed Tasks', value: completedTasks, color: 'bg-purple-500', icon: '✅' },
      ];

      loading = false;
    } catch (err) {
      error = err instanceof Error ? err.message : 'Failed to load statistics';
      loading = false;
    }
  }
</script>

<div class="container mx-auto px-4 py-8">
  <h1 class="text-3xl font-bold text-gray-800 mb-6">Dashboard</h1>

  {#if loading}
    <div class="flex items-center justify-center py-12">
      <div class="text-gray-600">Loading statistics...</div>
    </div>
  {:else if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
      Error: {error}
    </div>
  {:else}
    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      {#each stats as stat}
        <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">{stat.label}</p>
              <p class="text-3xl font-bold text-gray-800">{stat.value}</p>
            </div>
            <div class="w-12 h-12 rounded-lg {stat.color} flex items-center justify-center text-2xl">
              {stat.icon}
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}

  <!-- Welcome Card -->
  <div class="bg-white rounded-lg shadow p-6">
    <h2 class="text-xl font-semibold mb-4">Welcome to Task Management System</h2>
    <p class="text-gray-600 mb-4">
      This is a full-stack portfolio project demonstrating:
    </p>
    <ul class="list-disc list-inside space-y-2 text-gray-600">
      <li>Spring Boot 3 backend with RESTful API</li>
      <li>Svelte frontend with TypeScript</li>
      <li>Tailwind CSS for responsive styling</li>
      <li>Real-time data from backend</li>
      <li>CRUD operations with modal dialogs</li>
      <li>Docker containerization</li>
    </ul>

    <div class="mt-6 pt-6 border-t">
      <h3 class="font-semibold text-gray-800 mb-3">Quick Actions</h3>
      <div class="flex flex-wrap gap-3">
        <a 
          href="#/users" 
          class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition">
          Manage Users
        </a>
        <a 
          href="#/projects" 
          class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg transition">
          View Projects
        </a>
        <a 
          href="#/tasks" 
          class="bg-yellow-500 hover:bg-yellow-600 text-white px-4 py-2 rounded-lg transition">
          View Tasks
        </a>
      </div>
    </div>
  </div>
</div>