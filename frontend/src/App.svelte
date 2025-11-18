<script lang="ts">
  import { onMount } from 'svelte';
  import Router from 'svelte-spa-router';
  import { push, location } from 'svelte-spa-router';
  import { authStore } from './stores/authStore';

  import Login from './pages/Login.svelte';
  import Dashboard from './pages/Dashboard.svelte';
  import UserList from './pages/UserList.svelte';
  import ProjectList from './pages/ProjectList.svelte';
  import TaskList from './pages/TaskList.svelte';
  import Taskboard from './pages/Taskboard.svelte';
  import type { User } from './models/user';

  let currentUser: User | null = null;
  let isAuthenticated = false;

  authStore.subscribe(state => {
    currentUser = state.user;
    isAuthenticated = state.isAuthenticated;
  });

  onMount(() => {
    authStore.init();
  });

  // Guard function to protect routes
  $: if ($location !== '/login' && !isAuthenticated) {
    push('/login');
  }

  function handleLogout() {
    authStore.logout();
    push('/login');
  }

  const routes = {
    '/login': Login,
    '/': Dashboard,
    '/users': UserList,
    '/projects': ProjectList,
    '/tasks': TaskList,
    '/tasks/board': Taskboard,
  };
</script>

<div class="min-h-screen bg-gray-100">
  {#if isAuthenticated}
    <nav class="bg-white shadow">
      <div class="container mx-auto px-4">
        <div class="flex items-center justify-between h-16">
          <div class="flex items-center">
            <h1 class="text-xl font-bold text-gray-800">Task Management</h1>
          </div>
          
          <div class="flex items-center space-x-4">
            <a href="#/" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
              Dashboard
            </a>
            <a href="#/users" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
              Users
            </a>
            <a href="#/projects" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
              Projects
            </a>
            <a href="#/tasks" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
              Tasks
            </a>
            <a href="#/tasks/board" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
              Kanban
            </a>

            <div class="border-l pl-4 flex items-center gap-3">
              <div class="text-sm">
                <div class="font-medium text-gray-800">
                  {currentUser?.firstName} {currentUser?.lastName}
                </div>
                <div class="text-gray-500 text-xs">
                  {currentUser?.role}
                </div>
              </div>
              
              <button
                on:click={handleLogout}
                class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-2 rounded transition text-sm font-medium">
                Logout
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  {/if}

  <main>
    <Router {routes} />
  </main>
</div>