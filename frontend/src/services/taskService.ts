import type { Task, TaskCreate, TaskUpdate, TaskStatus } from '../models/task';
import { config } from '../lib/config';
import { apiRequest } from './apiClient'; 

const API_URL = `${config.apiUrl}/tasks`;

export async function getAllTasks(): Promise<Task[]> {
  const response = await apiRequest(API_URL);
  if (!response.ok) {
    throw new Error('Failed to fetch tasks');
  }
  return await response.json();
}

export async function getTaskById(id: number): Promise<Task> {
  const response = await apiRequest(`${API_URL}/${id}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch task ${id}`);
  }
  return await response.json();
}

export async function getTasksByProject(projectId: number): Promise<Task[]> {
  const response = await apiRequest(`${API_URL}/project/${projectId}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch tasks for project ${projectId}`);
  }
  return await response.json();
}

export async function getTasksByAssignee(assigneeId: number): Promise<Task[]> {
  const response = await apiRequest(`${API_URL}/assignee/${assigneeId}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch tasks for assignee ${assigneeId}`);
  }
  return await response.json();
}

export async function getTasksByStatus(status: TaskStatus): Promise<Task[]> {
  const response = await apiRequest(`${API_URL}/status/${status}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch tasks with status ${status}`);
  }
  return await response.json();
}

export async function createTask(task: TaskCreate): Promise<Task> {
  const response = await apiRequest(API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(task),
  });
  if (!response.ok) {
    throw new Error('Failed to create task');
  }
  return await response.json();
}

export async function updateTask(id: number, task: TaskUpdate): Promise<Task> {
  const response = await apiRequest(`${API_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(task),
  });
  if (!response.ok) {
    throw new Error(`Failed to update task ${id}`);
  }
  return await response.json();
}

export async function deleteTask(id: number): Promise<void> {
  const response = await apiRequest(`${API_URL}/${id}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to delete task ${id}`);
  }
}