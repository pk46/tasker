import { ApiClient } from './ApiClient';
import { UserResponseDTO } from './AuthApi';


export interface ProjectCreateDTO {
    name: string;
    description?: string;
}

export interface ProjectUpdateDTO {
    name?: string;
    description?: string;
}

export interface ProjectResponseDTO {
    id: number;
    name: string;
    description?: string;
    owner: UserResponseDTO;
    createdAt: string;
    updatedAt: string;
    taskCount: number;
}


export class ProjectsApi {
    constructor(private client: ApiClient) {}

    async create(data: ProjectCreateDTO): Promise<ProjectResponseDTO> {
        const response = await this.client.post('/api/projects', data);
        return this.client.parseResponse<ProjectResponseDTO>(response);
    }

    async getById(id: number): Promise<ProjectResponseDTO> {
        const response = await this.client.get(`/api/projects/${id}`);
        return this.client.parseResponse<ProjectResponseDTO>(response);
    }

    async getAll(): Promise<ProjectResponseDTO[]> {
        const response = await this.client.get('/api/projects');
        return this.client.parseResponse<ProjectResponseDTO[]>(response);
    }

    async getByOwnerId(ownerId: number): Promise<ProjectResponseDTO[]> {
        const response = await this.client.get(`/api/projects/owner/${ownerId}`);
        return this.client.parseResponse<ProjectResponseDTO[]>(response);
    }

    async update(id: number, data: ProjectUpdateDTO): Promise<ProjectResponseDTO> {
        const response = await this.client.put(`/api/projects/${id}`, data);
        return this.client.parseResponse<ProjectResponseDTO>(response);
    }

    async delete(id: number): Promise<void> {
        const response = await this.client.delete(`/api/projects/${id}`);

        if (!response.ok()) {
            const errorText = await response.text();
            throw new Error(
                `Failed to delete project: ${response.status()} ${response.statusText()}\n${errorText}`
            );
        }
    }
}
