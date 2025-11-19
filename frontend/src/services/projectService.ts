import type { Project, ProjectCreate, ProjectUpdate } from '../models/project';
import { getTasksByProject } from './taskService';
import { config } from '../lib/config';
import { apiRequest } from './apiClient'; 

const API_URL = `${config.apiUrl}/projects`;

export async function getAllProjects(): Promise<Project[]> {
  const response = await apiRequest(API_URL);
  if (!response.ok) {
    throw new Error('Failed to apiRequest projects');
  }
  return await response.json();
}

export async function getProjectById(id: number): Promise<Project> {
  const response = await apiRequest(`${API_URL}/${id}`);
  if (!response.ok) {
    throw new Error(`Failed to apiRequest project ${id}`);
  }
  return await response.json();
}

export async function getProjectsByOwner(ownerId: number): Promise<Project[]> {
  const response = await apiRequest(`${API_URL}/owner/${ownerId}`);
  if (!response.ok) {
    throw new Error(`Failed to apiRequest projects for owner ${ownerId}`);
  }
  return await response.json();
}

export async function createProject(project: ProjectCreate, ownerId: number): Promise<Project> {
  const response = await apiRequest(`${API_URL}?ownerId=${ownerId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(project),
  });
  if (!response.ok) {
    throw new Error('Failed to create project');
  }
  return await response.json();
}

export async function updateProject(id: number, project: ProjectUpdate): Promise<Project> {
  const response = await apiRequest(`${API_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(project),
  });
  if (!response.ok) {
    throw new Error(`Failed to update project ${id}`);
  }
  return await response.json();
}

export async function deleteProject(id: number): Promise<void> {
  const response = await apiRequest(`${API_URL}/${id}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to delete project ${id}`);
  }
}

export async function getTaskCount(id: number): Promise<number> {
  const response = await getTasksByProject(id);
  return response.length;
}