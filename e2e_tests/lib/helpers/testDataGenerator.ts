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
