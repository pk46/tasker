import { ApiClient } from './ApiClient';

export interface LoginRequest {
    username: string;
    password: string;
}

export interface UserResponseDTO {
    id: number;
    username: string;
    email: string;
    role: string;
}

export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
    type: string;
    user: UserResponseDTO;
}

export interface RefreshTokenRequest {
    refreshToken: string;
}


export class AuthApi {
    constructor(private client: ApiClient) {}

    async login(username: string, password: string): Promise<LoginResponse> {
        const response = await this.client.post('/api/auth/login', {
            username,
            password,
        });

        const data = await this.client.parseResponse<LoginResponse>(response);

        this.client.setToken(data.accessToken);
        this.client.setRefreshToken(data.refreshToken);

        this.client.setRefreshCallback(async () => {
            await this.refreshToken();
        });

        return data;
    }

    async refreshToken(): Promise<LoginResponse> {
        const currentRefreshToken = this.client.getRefreshToken();
        if (!currentRefreshToken) {
            throw new Error('No refresh token available');
        }

        const response = await this.client.post('/api/auth/refresh', {
            refreshToken: currentRefreshToken,
        });

        const data = await this.client.parseResponse<LoginResponse>(response);

        this.client.setToken(data.accessToken);
        this.client.setRefreshToken(data.refreshToken);

        return data;
    }
}
