import type { User, UserCreate, UserUpdate } from "../models/user";
import { config } from "../lib/config"

const API_URL = `${config.apiUrl}/users`;

export async function getAllUsers(): Promise<User[]> {
    const response = await fetch(API_URL);
    if (!response.ok) {
        throw new Error("Failed to fetch users;")
    }
    return await response.json();
}

export async function getUserById(userId: string): Promise<User> {
    const response = await fetch(`${API_URL}/${userId}`);
    if (!response.ok) {
        throw new Error(`Failed to fetch user ${userId}`);
    }
    return await response.json();
}

export async function createUser(user: UserCreate): Promise<User> {
    const response = await fetch(API_URL, {
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
    const response = await fetch(`${API_URL}/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user),
    });
    if (!response.ok) {
        throw new Error(`Failed to update user ${id}`);
    }
    return await response.json();
}

export async function deleteUser(id: number): Promise<void> {
    const response = await fetch(`${API_URL}/${id}`, {
        method: "DELETE",
    });
    if (!response.ok) {
        throw new Error(`Failed to delete user ${id}`);
    }
}
