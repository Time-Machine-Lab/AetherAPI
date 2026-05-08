export const PLATFORM_ADMIN_ROLES = ['OWNER', 'ADMIN', 'PLATFORM_ADMIN'] as const

export function isPlatformAdminRole(role?: string | null): boolean {
  if (!role) return false
  const normalized = role.trim().toUpperCase()
  return PLATFORM_ADMIN_ROLES.includes(normalized as (typeof PLATFORM_ADMIN_ROLES)[number])
}
