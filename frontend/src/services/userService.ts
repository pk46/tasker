import type { User, UserCreate, UserUpdate } from "../models/user";
import { config } from "../lib/config"
import { apiRequest } from './apiClient'; 

const API_URL = `${config.apiUrl}/users`;

export async function getAllUsers(): Promise<User[]> {
    const response = await apiRequest(API_URL);
    if (!response.ok) {
        if (response.status === 403) {
            throw new Error("Access denied - you don't have permission to view users")
        }
        throw new Error("Failed to apiRequest users;")
    }
    return await response.json();
}

export async function getUserById(userId: string): Promise<User> {
    const response = await apiRequest(`${API_URL}/${userId}`);
    if (!response.ok) {
        throw new Error(`Failed to apiRequest user ${userId}`);
    }
    return await response.json();
}

export async function createUser(user: UserCreate): Promise<User> {
    const response = await apiRequest(API_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user),
    });
    if (!response.ok) {
        throw new Error('Failed to create user');
    }
    return await response.json();
}

export async function updateUser(id: number, user: UserUpdate): Promise<User> {
    const response = await apiRequest(`${API_URL}/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user),
    });
    if (!response.ok) {
        if (response.status === 403) {
            throw new Error('Access denied - you do not have permission to update this user');
        }
        throw new Error(`Failed to update user ${id}`);
    }
    return await response.json();
}

export async function deleteUser(id: number): Promise<void> {
    const response = await apiRequest(`${API_URL}/${id}`, {
        method: "DELETE",
    });
    if (!response.ok) {
        if (response.status === 403) {
            throw new Error('Access denied - only administrators can delete users');
        }
    
        if (response.status === 404) {
        throw new Error('User not found');
        }
        
        throw new Error(`Failed to delete user ${id}`);
    }
}
