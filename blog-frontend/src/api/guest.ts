import axios from './index'
import type { GuestLoginResponseDTO, Result } from '@/types'

export async function guestLogin(): Promise<Result<GuestLoginResponseDTO>> {
  const response = await axios.post('/v1/auth/guest-login')
  return response.data
}

export async function deleteGuest(): Promise<Result<void>> {
  const response = await axios.delete('/v1/auth/guest')
  return response.data
}
