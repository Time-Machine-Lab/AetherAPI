// DTOs aligned with docs/api/console-auth.yaml
// Note: the backend returns the inner data directly (no TmlResult envelope)

export interface ConsoleSignInReqDto {
  loginName: string
  password: string
}

export interface ConsoleCurrentUserDto {
  userId: string
  loginName: string
  displayName: string
  email: string
  role: string
}

export interface ConsoleSignInRespDto {
  accessToken: string
  tokenType: string
  expiresAt: string
  expiresInSeconds: number
  currentUser: ConsoleCurrentUserDto
}

export interface ConsoleCurrentSessionRespDto {
  currentUser: ConsoleCurrentUserDto
}
