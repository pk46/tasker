import type { User } from './user';

export interface Project {
  id: number;
  name: string;
  description: string;
  owner: User;
  createdAt: string;
  updatedAt: string;
  taskCount: number;
}

export interface ProjectCreate {
  name: string;
  description?: string;
}

export interface ProjectUpdate {
  name?: string;
  description?: string;
}