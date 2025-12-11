import { ApiClient } from './ApiClient';
import { UserResponseDTO } from './AuthApi';

export enum TaskStatus {
    TODO = 'TODO',
    IN_PROGRESS = 'IN_PROGRESS',
    DONE = 'DONE',
}

export enum TaskPriority {
    LOW = 'LOW',
    MEDIUM = 'MEDIUM',
    HIGH = 'HIGH',
}

export interface TaskCreateDTO {
    title: string;
    description?: string;
    status?: TaskStatus;
    priority?: TaskPriority;
    dueDate?: string; // ISO date format (YYYY-MM-DD)
    projectId: number;
    assigneeId?: number;
}

export interface TaskUpdateDTO {
    title?: string;
    description?: string;
    status?: TaskStatus;
    priority?: TaskPriority;
    dueDate?: string; // ISO date format (YYYY-MM-DD)
    assigneeId?: number;
}

export interface TaskResponseDTO {
    id: number;
    title: string;
    description?: string;
    status: TaskStatus;
    priority: TaskPriority;
    dueDate?: string;
    projectId: number;
    projectName: string;
    assignee?: UserResponseDTO;
    createdAt: string;
    updatedAt: string;
}

export class TaskApi {
    constructor(private client: ApiClient) {}

     async create(data: TaskCreateDTO): Promise<TaskResponseDTO> {
        const response = await this.client.post('/api/tasks', data);
        return this.client.parseResponse<TaskResponseDTO>(response);
    }

    async getById(id: number): Promise<TaskResponseDTO> {
        const response = await this.client.get(`/api/tasks/${id}`);
        return this.client.parseResponse<TaskResponseDTO>(response);
    }

    async getAll(): Promise<TaskResponseDTO[]> {
        const response = await this.client.get('/api/tasks');
        return this.client.parseResponse<TaskResponseDTO[]>(response);
    }

    async getByStatus(status: TaskStatus): Promise<TaskResponseDTO[]> {
        const response = await this.client.get(`/api/tasks/status/${status}`);
        return this.client.parseResponse<TaskResponseDTO[]>(response);
    }

    async getByProjectId(projectId: number): Promise<TaskResponseDTO[]> {
        const response = await this.client.get(`/api/tasks/project/${projectId}`);
        return this.client.parseResponse<TaskResponseDTO[]>(response);
    }

    async getByAssigneeId(assigneeId: number): Promise<TaskResponseDTO[]> {
        const response = await this.client.get(`/api/tasks/assignee/${assigneeId}`);
        return this.client.parseResponse<TaskResponseDTO[]>(response);
    }

    async update(id: number, data: TaskUpdateDTO): Promise<TaskResponseDTO> {
        const response = await this.client.put(`/api/tasks/${id}`, data);
        return this.client.parseResponse<TaskResponseDTO>(response);
    }

    async delete(id: number): Promise<void> {
        const response = await this.client.delete(`/api/tasks/${id}`);

        if (!response.ok()) {
            const errorText = await response.text();
            throw new Error(
                `Failed to delete task: ${response.status()} ${response.statusText()}\n${errorText}`
            );
        }
    }
}
