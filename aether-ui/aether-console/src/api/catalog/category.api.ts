import { http } from '@/api/http'
import type { CategoryDto, CreateCategoryBody, PageDto, RenameCategoryBody } from './catalog.dto'
import type { ApiCategory, PageResult } from './catalog.types'

function mapCategory(dto: CategoryDto): ApiCategory {
  return {
    categoryCode: dto.categoryCode,
    name: dto.name,
    status: dto.status,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  }
}

export async function listCategories(params?: {
  page?: number
  pageSize?: number
}): Promise<PageResult<ApiCategory>> {
  const { data } = await http.get<PageDto<CategoryDto>>('v1/categories', { params })
  return {
    items: data.items.map(mapCategory),
    total: data.total,
    page: data.page,
    pageSize: data.pageSize,
  }
}

export async function createCategory(body: CreateCategoryBody): Promise<ApiCategory> {
  const { data } = await http.post<CategoryDto>('v1/categories', body)
  return mapCategory(data)
}

export async function renameCategory(
  categoryCode: string,
  body: RenameCategoryBody,
): Promise<ApiCategory> {
  const { data } = await http.patch<CategoryDto>(`v1/categories/${categoryCode}`, body)
  return mapCategory(data)
}

export async function enableCategory(categoryCode: string): Promise<ApiCategory> {
  const { data } = await http.post<CategoryDto>(`v1/categories/${categoryCode}/enable`)
  return mapCategory(data)
}

export async function disableCategory(categoryCode: string): Promise<ApiCategory> {
  const { data } = await http.post<CategoryDto>(`v1/categories/${categoryCode}/disable`)
  return mapCategory(data)
}
