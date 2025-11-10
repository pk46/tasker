<script lang="ts">
  let count: number = 0;
  
  function increment() {
    count += 1;
  }
</script>

<div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
  <div class="bg-white rounded-lg shadow-xl p-8 max-w-md w-full">
    <h1 class="text-3xl font-bold text-gray-800 mb-4">
      Task Management System
    </h1>
    
    <p class="text-gray-600 mb-6">
      Welcome to Svelte + Tailwind CSS
    </p>
    
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-6">
      <p class="text-2xl font-semibold text-blue-900 mb-2">
        Count: {count}
      </p>
      <button 
        on:click={increment}
        class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded-lg transition duration-200">
        Increment
      </button>
    </div>
    
    <div class="space-y-2">
      <div class="flex items-center gap-2">
        <div class="w-3 h-3 bg-green-500 rounded-full"></div>
        <span class="text-sm text-gray-600">Svelte running</span>
      </div>
      <div class="flex items-center gap-2">
        <div class="w-3 h-3 bg-green-500 rounded-full"></div>
        <span class="text-sm text-gray-600">Tailwind CSS active</span>
      </div>
      <div class="flex items-center gap-2">
        <div class="w-3 h-3 bg-green-500 rounded-full"></div>
        <span class="text-sm text-gray-600">TypeScript enabled</span>
      </div>
    </div>
  </div>
</div>