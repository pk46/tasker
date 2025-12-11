import { ApiClient } from './ApiClient';

export enum UserRole {
    USER = 'USER',
    ADMIN = 'ADMIN',
}

export interface UserCreateDTO {
    username: string;
    email: string;
    password: string;
    role: UserRole;
    firstName?: string;
    lastName?: string;
}

export interface UserUpdateDTO {
    email?: string;
    password?: string;
    firstName?: string;
    lastName?: string;
    role?: UserRole;
}

export interface UserResponseDTO {
    id: number;
    username: string;
    email: string;
    role: UserRole;
    firstName?: string;
    lastName?: string;
    createdAt?: string;
    updatedAt?: string;
}

export class UsersApi {
    constructor(private client: ApiClient) {}

    async create(data: UserCreateDTO): Promise<UserResponseDTO> {
        const response = await this.client.post('/api/users', data);
        return this.client.parseResponse<UserResponseDTO>(response);
    }

    async getById(id: number): Promise<UserResponseDTO> {
        const response = await this.client.get(`/api/users/${id}`);
        return this.client.parseResponse<UserResponseDTO>(response);
    }

    async getAll(): Promise<UserResponseDTO[]> {
        const response = await this.client.get('/api/users');
        return this.client.parseResponse<UserResponseDTO[]>(response);
    }

    async update(id: number, data: UserUpdateDTO): Promise<UserResponseDTO> {
        const response = await this.client.put(`/api/users/${id}`, data);
        return this.client.parseResponse<UserResponseDTO>(response);
    }

     async delete(id: number): Promise<void> {
        const response = await this.client.delete(`/api/users/${id}`);

        if (!response.ok()) {
            const errorText = await response.text();
            throw new Error(
                `Failed to delete user: ${response.status()} ${response.statusText()}\n${errorText}`
            );
        }
    }
}
