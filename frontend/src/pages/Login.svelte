<script lang="ts">
  import { push } from 'svelte-spa-router';
  import { authStore } from '../stores/authStore';

  let username = '';
  let password = '';
  let error = '';
  let loading = false;

  async function handleLogin() {
    if (!username || !password) {
      error = 'Username and password are required';
      return;
    }

    try {
      loading = true;
      error = '';

    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username,
        password: password,
      }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      error = errorData?.message || 'Invalid username or password';
      return;
    }

    const data = await response.json();

    localStorage.setItem('jwt_token', data.token);

    authStore.login(data.user);

    push('/');
    } catch (err) {
      error = 'Login failed. Please try again.';
      console.error('Login error:', err);
    } finally {
      loading = false;
    }
  }
</script>

<div class="min-h-screen bg-gray-100 flex items-center justify-center px-4">
  <div class="max-w-md w-full">
    <div class="bg-white rounded-lg shadow-lg p-8">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-800 mb-2">Task Management</h1>
        <p class="text-gray-600">Sign in to your account</p>
      </div>

      <form on:submit|preventDefault={handleLogin}>
        {#if error}
          <div class="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        {/if}

        <div class="space-y-4">
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 mb-1">
              Username
            </label>
            <input
              type="text"
              id="username"
              bind:value={username}
              disabled={loading}
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100"
              placeholder="Enter your username"
              autocomplete="username"
            />
          </div>

          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
              Password
            </label>
            <input
              type="password"
              id="password"
              bind:value={password}
              disabled={loading}
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100"
              placeholder="Enter your password"
              autocomplete="current-password"
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          class="w-full mt-6 bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed">
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </form>

      <div class="mt-6 text-center text-sm text-gray-600">
        <p class="mb-2">Demo account:</p>
        <p class="text-xs">
          Use username: admin<br />
          Password: admin
        </p>
        <p>If user does not exists, rerun backend with --init-data argument</p>
      </div>
    </div>
  </div>
</div>