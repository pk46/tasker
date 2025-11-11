<script lang="ts">
  import Router from 'svelte-spa-router';
  import Dashboard from './pages/Dashboard.svelte';
  import UserList from './pages/UserList.svelte';

  const routes = {
    '/': Dashboard,
    '/users': UserList,
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
          <a href="#/" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded">
            Dashboard
          </a>
          <a href="#/users" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded">
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