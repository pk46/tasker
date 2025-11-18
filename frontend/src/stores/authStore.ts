import { writable } from 'svelte/store';
import type { User } from '../models/user';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
}

function createAuthStore() {
  const { subscribe, set, update } = writable<AuthState>({
    user: null,
    isAuthenticated: false
  });

  return {
    subscribe,
    login: (user: User) => {
      set({ user, isAuthenticated: true });
      localStorage.setItem('currentUser', JSON.stringify(user));
    },
    logout: () => {
      set({ user: null, isAuthenticated: false });
      localStorage.removeItem('currentUser');
    },
    init: () => {
      const storedUser = localStorage.getItem('currentUser');
      if (storedUser) {
        const user = JSON.parse(storedUser);
        set({ user, isAuthenticated: true });
      }
    }
  };
}

export const authStore = createAuthStore();