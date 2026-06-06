import axios from './index'
import type { AboutDTO, UpdateAboutDTO, Result } from '@/types'

export async function getAbout(): Promise<Result<AboutDTO>> {
  const response = await axios.get('/v1/about')
  return response.data
}

export async function updateAbout(dto: UpdateAboutDTO): Promise<Result<AboutDTO>> {
  const response = await axios.put('/v1/about', dto)
  return response.data
}
