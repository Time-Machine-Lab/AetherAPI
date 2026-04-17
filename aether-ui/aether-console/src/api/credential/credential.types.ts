export type CredentialStatus = 'ENABLED' | 'DISABLED' | 'REVOKED' | 'EXPIRED'

export interface LastUsedSnapshot {
  lastUsedAt: string | null
  lastUsedChannel: string | null
  lastUsedResult: string | null
}

export interface ApiKey {
  credentialId: string
  credentialCode: string
  credentialName: string
  credentialDescription: string | null
  maskedKey: string
  keyPrefix: string
  status: CredentialStatus
  expireAt: string | null
  revokedAt: string | null
  createdAt: string
  updatedAt: string
  lastUsedSnapshot: LastUsedSnapshot | null
}

export interface IssuedApiKey extends ApiKey {
  plaintextKey: string
}
