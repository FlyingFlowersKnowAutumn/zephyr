import axios from './index'
import type { LoginDTO, RegisterDTO, UserDTO, LoginResponseDTO, Result } from '@/types'

export async function login(dto: LoginDTO): Promise<Result<LoginResponseDTO>> {
  const response = await axios.post('/api/v1/auth/login', dto)
  return response.data
}

export async function register(dto: RegisterDTO): Promise<Result<UserDTO>> {
  const response = await axios.post('/api/v1/auth/register', dto)
  return response.data
}

export async function getCurrentUser(): Promise<Result<UserDTO>> {
  const response = await axios.get('/api/v1/auth/me')
  return response.data
}

export async function logout(refreshToken: string): Promise<Result<void>> {
  const response = await axios.post('/api/v1/auth/logout', { refreshToken })
  return response.data
}
