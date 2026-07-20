import axios from './index'
import type { Result } from '@/types'

export interface SiteStatsDTO {
  visitorCount: number
  viewCount: number
  articleCount: number
  lastUpdate: string
}

export async function getSiteStats(): Promise<Result<SiteStatsDTO>> {
  const response = await axios.get('/v1/site/stats')
  return response.data
}

export async function incrementViewCount(): Promise<Result<void>> {
  const response = await axios.post('/v1/site/view')
  return response.data
}
