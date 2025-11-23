import { writable } from 'svelte/store';
import type { User } from '../models/user';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
}

function createAuthStore() {
  const { subscribe, set, update } = writable<AuthState>({
    user: null,
    isAuthenticated: false,
  });

  return {
    subscribe,
    login: (user: User) => {
      set({ user, isAuthenticated: true });
      localStorage.setItem('currentUser', JSON.stringify(user));
    },
    logout: () => {
      set({ user: null, isAuthenticated: false });
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('currentUser');
    },
    checkAuth: () => {
      const token = localStorage.getItem('access_token');
      const userJson = localStorage.getItem('currentUser');
      
      if (token && userJson) {
        try {
          const user = JSON.parse(userJson);
          set({ user, isAuthenticated: true });
        } catch (e) {
          set({ user: null, isAuthenticated: false });
        }
      }
    },
  };
}

export const authStore = createAuthStore();