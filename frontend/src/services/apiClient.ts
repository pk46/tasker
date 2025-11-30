import { config } from "../lib/config"

let isRefreshing = false;
let refreshPromise: Promise<string> | null = null;

async function refreshAccessToken(): Promise<string> {
  const refreshToken = localStorage.getItem('refresh_token');
  console.log('[apiClient] Attempting to refresh token, refresh token present:', !!refreshToken);

  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  const refreshUrl = `${config.apiUrl}/auth/refresh`;
  console.log('[apiClient] Calling refresh endpoint:', refreshUrl);

  const response = await fetch(refreshUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refreshToken }),
  });

  console.log('[apiClient] Refresh response status:', response.status);

  if (!response.ok) {
    throw new Error('Refresh token invalid');
  }

  const data = await response.json();
  console.log('[apiClient] Token refresh successful');

  localStorage.setItem('access_token', data.accessToken);
  localStorage.setItem('refresh_token', data.refreshToken);

  return data.accessToken;
}

export async function apiRequest(
  endpoint: string,
  options: RequestInit = {}
): Promise<Response> {

  let token = localStorage.getItem('access_token');
  console.log('[apiClient] Making request to:', endpoint, 'with token:', token ? 'present' : 'missing');

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  let response = await fetch(endpoint, {
    ...options,
    headers,
  });

  console.log('[apiClient] Response status:', response.status, 'for endpoint:', endpoint);

  if (response.status === 401) {
    console.log(`[apiClient] Got ${response.status}, attempting token refresh`);
    try {
      if (isRefreshing && refreshPromise) {
        token = await refreshPromise;
      } else {
        isRefreshing = true;
        refreshPromise = refreshAccessToken();
        token = await refreshPromise;
        isRefreshing = false;
        refreshPromise = null;
      }

      headers['Authorization'] = `Bearer ${token}`;
      response = await fetch(endpoint, {
        ...options,
        headers,
      });
    } catch (error) {
      console.error('Token refresh failed:', error);
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('currentUser');
      window.location.href = '/#/login';
      throw error;
    }
  }

  if (response.status === 403) {
    console.warn('[apiClient] Access denied (403) - insufficient permissions');
  }

  return response;
}