export interface LastUsedSnapshotDto {
  lastUsedAt: string | null
  lastUsedChannel: string | null
  lastUsedResult: string | null
}

export interface CurrentUserApiKeyDto {
  credentialId: string
  credentialCode: string
  credentialName: string
  credentialDescription: string | null
  maskedKey: string
  keyPrefix: string
  status: 'ENABLED' | 'DISABLED' | 'REVOKED' | 'EXPIRED'
  expireAt: string | null
  revokedAt: string | null
  createdAt: string
  updatedAt: string
  lastUsedSnapshot: LastUsedSnapshotDto | null
}

export interface IssuedCurrentUserApiKeyDto extends CurrentUserApiKeyDto {
  plaintextKey: string
}

export interface CurrentUserApiKeyPageDto {
  items: CurrentUserApiKeyDto[]
  page: number
  size: number
  total: number
}

export interface CreateCurrentUserApiKeyBody {
  credentialName: string
  credentialDescription?: string | null
  expireAt?: string | null
}
