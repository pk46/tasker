<script lang="ts">
  import Router from 'svelte-spa-router';
  import Dashboard from './pages/Dashboard.svelte';
  import UserList from './pages/UserList.svelte';
  import ProjectList from './pages/ProjectList.svelte';
  import TaskList from './pages/TaskList.svelte';
  import Taskboard from './pages/Taskboard.svelte';

  const routes = {
    '/': Dashboard,
    '/users': UserList,
    '/projects': ProjectList,
    '/tasks': TaskList,
    '/tasks/board': Taskboard,
  };
</script>

<div class="min-h-screen bg-gray-100">
  <nav class="bg-white shadow">
    <div class="container mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        <div class="flex items-center">
          <h1 class="text-xl font-bold text-gray-800">Task Management</h1>
        </div>
        <div class="flex space-x-4">
          <a href="#/" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
            Dashboard
          </a>
           <a href="#/tasks/board" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
            Kanban
          </a>
          <a href="#/projects" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
            Projects
          </a>
          <a href="#/tasks" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
            Tasks
          </a>
          <a href="#/users" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded transition">
            Users
          </a>
        </div>
      </div>
    </div>
  </nav>

  <main>
    <Router {routes} />
  </main>
</div>