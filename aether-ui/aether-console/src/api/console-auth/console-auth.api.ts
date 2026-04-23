import { http } from '@/api/http'
import type {
  ConsoleSignInReqDto,
  ConsoleSignInRespDto,
  ConsoleCurrentSessionRespDto,
} from './console-auth.dto'
import type { ConsoleCurrentUser, ConsoleSession } from './console-auth.types'

function mapCurrentUser(dto: ConsoleSignInRespDto['currentUser']): ConsoleCurrentUser {
  return {
    userId: dto.userId,
    loginName: dto.loginName,
    displayName: dto.displayName,
    email: dto.email,
    role: dto.role,
  }
}

function mapSession(dto: ConsoleSignInRespDto): ConsoleSession {
  return {
    accessToken: dto.accessToken,
    expiresAt: dto.expiresAt,
    currentUser: mapCurrentUser(dto.currentUser),
  }
}

export async function signInConsole(body: ConsoleSignInReqDto): Promise<ConsoleSession> {
  const { data } = await http.post<ConsoleSignInRespDto>('/console/auth/sign-in', body)
  return mapSession(data)
}

export async function getCurrentConsoleSession(): Promise<ConsoleCurrentUser> {
  const { data } = await http.get<ConsoleCurrentSessionRespDto>('/console/auth/current-session')
  return mapCurrentUser(data.currentUser)
}
