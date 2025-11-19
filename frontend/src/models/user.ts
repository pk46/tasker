export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'USER' | 'ADMIN';
}

export interface UserCreate {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  role: string; 
}

export interface UserUpdate {
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  role?: string;
}