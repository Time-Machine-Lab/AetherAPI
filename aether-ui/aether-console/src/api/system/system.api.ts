import { http } from '@/api/http'
import type { SystemStatusDto } from '@/api/system/system.dto'
import type { SystemStatus } from '@/api/system/system.types'

export async function getSystemStatus() {
  const { data } = await http.get<SystemStatusDto>('v1/system/status')

  return mapSystemStatus(data)
}

function mapSystemStatus(dto: SystemStatusDto): SystemStatus {
  return {
    available: dto.available,
    environment: dto.environment,
    service: dto.service,
  }
}
