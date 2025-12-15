
import { CreateUserParams, EditUserParams } from "../../pages/modals/CreateUserModal";
import { UserRole } from "../api/UsersApi";

export function generateUniqueId(): string {
    const timestamp = Date.now();
    return `${timestamp}`;
}

export function generateUniqueUsername(name: string): string {
    return `autotest_${name}_${generateUniqueId()}`;
}

export function generateUniqueEmail(): string {
    return `auto_${generateUniqueId()}@test.cz`;
}

export function createDefaultUserDto(
    prefix: string = "users", 
    overrides: Partial<CreateUserParams> = {}
): CreateUserParams {
    return {
        username: generateUniqueUsername(prefix),
        email: generateUniqueEmail(),
        password: "autotestpass",
        userRole: UserRole.USER,
        ...overrides
    };
}

export function createEdituserDto(
    overrides: Partial<EditUserParams> = {}
): EditUserParams {
    return {
        email: generateUniqueEmail(),
        ...overrides
    };
}