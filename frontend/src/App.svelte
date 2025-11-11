<script lang="ts">
  let count = 0;

  function increment() {
    count += 1;
  }
</script>

<main class="min-h-screen bg-gray-100 flex items-center justify-center p-4">
  <div class="bg-white rounded-lg shadow-lg p-8 max-w-md w-full">
    <h1 class="text-3xl font-bold text-gray-800 mb-4">
      Svelte + Tailwind Test
    </h1>
    
    <p class="text-gray-600 mb-6">
      Pokud vidíš styly, Tailwind funguje!
    </p>
    
    <div class="bg-blue-100 border-2 border-blue-300 rounded-lg p-4 mb-4">
      <p class="text-xl font-semibold mb-2">Count: {count}</p>
      <button 
        on:click={increment}
        class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded">
        Click me
      </button>
    </div>

    <div class="text-sm text-gray-500">
      <p>✅ Svelte running</p>
      <p>✅ TypeScript enabled</p>
      <p>✅ Tailwind CSS active</p>
    </div>
  </div>
</main>