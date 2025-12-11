import { APIRequestContext, APIResponse } from '@playwright/test';

interface JWTPayload {
    exp: number;
    [key: string]: number;
}

function decodeJWT(token: string): JWTPayload | null {
    try {
        const parts = token.split('.');
        if (parts.length !== 3) return null;

        const payload = parts[1];
        const decoded = Buffer.from(payload, 'base64').toString('utf-8');
        return JSON.parse(decoded);
    } catch {
        return null;
    }
}

export class ApiClient {
    private baseURL: string;
    private refreshToken?: string;
    private refreshCallback!: () => Promise<void>;

    constructor(
        private request: APIRequestContext,
        private token?: string
    ) {
        this.baseURL = process.env.API_BASE_URL || 'http://localhost:8080';
    }

    setToken(token: string): void {
        this.token = token;
    }

    getToken(): string | undefined {
        return this.token;
    }

    setRefreshToken(refreshToken: string): void {
        this.refreshToken = refreshToken;
    }

    getRefreshToken(): string | undefined {
        return this.refreshToken;
    }

    setRefreshCallback(callback: () => Promise<void>): void {
        this.refreshCallback = callback;
    }

    private isTokenExpiringSoon(): boolean {
        if (!this.token) return false;

        const payload = decodeJWT(this.token);
        if (!payload || !payload.exp) return false;

        const expirationTime = payload.exp * 1000;
        const now = Date.now();
        const timeUntilExpiration = expirationTime - now;

        return timeUntilExpiration < 60000;
    }

    private async ensureValidToken(): Promise<void> {
        if (this.isTokenExpiringSoon()) {
            await this.refreshCallback();
        }
    }

    private getHeaders(customHeaders?: Record<string, string>): Record<string, string> {
        const headers: Record<string, string> = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...customHeaders,
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        return headers;
    }

    private getUrl(endpoint: string): string {
        const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint;
        return `${this.baseURL}/${cleanEndpoint}`;
    }

    private async makeRequest(
        method: 'GET' | 'POST' | 'PUT' | 'DELETE',
        endpoint: string,
        options?: {
            data?: any;
            headers?: Record<string, string>;
            params?: Record<string, string | number | boolean>;
        }
    ): Promise<APIResponse> {
        await this.ensureValidToken();

        const url = this.getUrl(endpoint);
        const headers = this.getHeaders(options?.headers);

        const response = await this.request.fetch(url, {
            method,
            headers,
            data: options?.data,
            params: options?.params,
        });

        return response;
    }

    async get(
        endpoint: string,
        options?: {
            headers?: Record<string, string>;
            params?: Record<string, string | number | boolean>;
        }
    ): Promise<APIResponse> {
        return this.makeRequest('GET', endpoint, options);
    }

    async post(
        endpoint: string,
        data?: any,
        options?: {
            headers?: Record<string, string>;
        }
    ): Promise<APIResponse> {
        return this.makeRequest('POST', endpoint, { data, ...options });
    }

    async put(
        endpoint: string,
        data?: any,
        options?: {
            headers?: Record<string, string>;
        }
    ): Promise<APIResponse> {
        return this.makeRequest('PUT', endpoint, { data, ...options });
    }

    async delete(
        endpoint: string,
        options?: {
            headers?: Record<string, string>;
        }
    ): Promise<APIResponse> {
        return this.makeRequest('DELETE', endpoint, options);
    }

    async parseResponse<T>(response: APIResponse): Promise<T> {
        if (!response.ok()) {
            const errorText = await response.text();
            throw new Error(
                `API request failed: ${response.status()} ${response.statusText()}\n${errorText}`
            );
        }

        return response.json() as Promise<T>;
    }
}
