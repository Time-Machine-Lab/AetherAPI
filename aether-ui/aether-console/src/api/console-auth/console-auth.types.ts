// Domain types for the console session auth feature

export interface ConsoleCurrentUser {
  userId: string
  loginName: string
  displayName: string
  email: string
  role: string
}

export interface ConsoleSession {
  accessToken: string
  expiresAt: string
  currentUser: ConsoleCurrentUser
}

export type ConsoleAuthErrorCode =
  | 'CONSOLE_SIGN_IN_REQUEST_INVALID'
  | 'CONSOLE_SIGN_IN_CREDENTIALS_INVALID'
  | 'CONSOLE_SESSION_UNAUTHORIZED'
