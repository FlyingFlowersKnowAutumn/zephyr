import axios from './index'
import type { LoginDTO, RegisterDTO, UserDTO, LoginResponseDTO, UpdatePasswordDTO, SendCodeDTO, RefreshTokenDTO, GuestLoginDTO, Result } from '@/types'

export async function login(dto: LoginDTO): Promise<Result<LoginResponseDTO>> {
  const response = await axios.post('/v1/auth/login', dto)
  return response.data
}

export async function register(dto: RegisterDTO): Promise<Result<UserDTO>> {
  const response = await axios.post('/v1/auth/register', dto)
  return response.data
}

export async function getCurrentUser(): Promise<Result<UserDTO>> {
  const response = await axios.get('/v1/auth/me')
  return response.data
}

export async function logout(refreshToken: string): Promise<Result<void>> {
  const response = await axios.post('/v1/auth/logout', { refreshToken })
  return response.data
}

/**
 * 修改密码（邮箱+验证码验证）
 */
export async function updatePassword(dto: UpdatePasswordDTO): Promise<Result<void>> {
  const response = await axios.put('/v1/auth/password', dto)
  return response.data
}

/**
 * 发送验证码
 * @param dto.email 邮箱地址
 * @param dto.type 验证码类型：REGISTER(注册) / FORGOT_PASSWORD(修改密码)
 */
/**
 * 免密登录（邮箱+验证码），未注册自动创建账号
 */
export async function guestLogin(dto: GuestLoginDTO): Promise<Result<LoginResponseDTO>> {
  const response = await axios.post('/v1/auth/guest-login', dto)
  return response.data
}

export async function sendCode(dto: SendCodeDTO): Promise<Result<void>> {
  const response = await axios.post('/v1/auth/send-code', dto)
  return response.data
}

/**
 * 刷新AccessToken
 */
export async function refreshToken(dto: RefreshTokenDTO): Promise<Result<{ token: string; expiresIn: number }>> {
  const response = await axios.post('/v1/auth/refresh', dto)
  return response.data
}
