export interface UserDTO {
  id: string
  username: string
  email: string
  avatarUrl: string
  role: string
  isGuest: boolean
  createdAt: string
}

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  email: string
  password: string
}

export interface LoginResponseDTO {
  token: string
  refreshToken: string
  user: UserDTO
}

export interface GuestLoginResponseDTO {
  token: string
  userId: string
  displayName: string
  role: string
  expiresAt: string
}

export interface Result<T> {
  code: number
  message: string
  data: T
  traceId: string
}
