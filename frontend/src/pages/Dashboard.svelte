<script lang="ts">
  let stats = [
    { label: 'Total Users', value: 0, color: 'bg-blue-500' },
    { label: 'Total Projects', value: 0, color: 'bg-green-500' },
    { label: 'Total Tasks', value: 0, color: 'bg-yellow-500' },
    { label: 'Completed', value: 0, color: 'bg-purple-500' },
  ];
</script>

<div class="container mx-auto px-4 py-8">
  <h1 class="text-3xl font-bold text-gray-800 mb-6">Dashboard</h1>

  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
    {#each stats as stat}
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="w-12 h-12 rounded-lg {stat.color} flex items-center justify-center">
              <span class="text-white text-2xl font-bold">{stat.value}</span>
            </div>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">{stat.label}</p>
          </div>
        </div>
      </div>
    {/each}
  </div>

  <div class="bg-white rounded-lg shadow p-6">
    <h2 class="text-xl font-semibold mb-4">Welcome to Task Management System</h2>
    <p class="text-gray-600 mb-4">
      This is a full-stack portfolio project demonstrating:
    </p>
    <ul class="list-disc list-inside space-y-2 text-gray-600">
      <li>Spring Boot 3 backend with RESTful API</li>
      <li>Svelte frontend with TypeScript</li>
      <li>Tailwind CSS for styling</li>
      <li>Responsive design</li>
      <li>Docker containerization</li>
    </ul>
  </div>
</div>