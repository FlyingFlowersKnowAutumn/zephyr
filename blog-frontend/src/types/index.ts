export interface UserDTO {
  id: string
  username: string
  displayName?: string
  email: string
  avatarUrl: string
  role: string
  bio?: string        // 个性签名
  status?: string     // 账户状态
  lastLoginAt?: string // 最后登录时间
  createdAt: string
}

export interface LoginDTO {
  username: string    // 用户名或邮箱
  password: string
}

export interface RegisterDTO {
  username: string
  email: string
  password: string
  code: string        // 邮箱验证码（必填）
}

/**
 * 修改密码（邮箱+验证码）
 */
export interface UpdatePasswordDTO {
  email: string       // 邮箱
  code: string        // 验证码
  newPassword: string // 新密码
}

/**
 * 发送验证码
 */
export interface SendCodeDTO {
  email: string        // 邮箱
  type: 'REGISTER' | 'FORGOT_PASSWORD' // 验证码类型
}

/**
 * 刷新Token
 */
export interface RefreshTokenDTO {
  refreshToken: string
}

export interface LoginResponseDTO {
  token: string
  refreshToken: string
  user: UserDTO
}

export interface Result<T> {
  code: number
  message: string
  data: T
  traceId: string
}

export interface ArticleDTO {
  id: string
  slug?: string
  title: string
  content: string
  excerpt: string
  coverImage: string
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  viewCount: number
  likeCount: number
  commentCount: number
  authorId: string
  authorName: string
  categoryId?: string
  categoryName?: string
  tags: string[]
  createdAt: string
  updatedAt: string
  publishedAt?: string
}

export interface CreateArticleDTO {
  title: string
  content: string
  slug?: string
  excerpt?: string
  coverImage?: string
  status: 'DRAFT' | 'PUBLISHED'
  categoryId?: string
  tags: string[]
}

export interface UpdateArticleDTO {
  title?: string
  content?: string
  excerpt?: string
  coverImage?: string
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  categoryId?: string
  tags?: string[]
}

export interface ArticleListDTO {
  content: ArticleDTO[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface CategoryDTO {
  id: string
  name: string
  description: string
  articleCount: number
  createdAt: string
}

export interface TagDTO {
  id: string
  name: string
  articleCount: number
  visitCount: number
  createdAt: string
}

export interface CommentDTO {
  id: string
  articleId: string
  articleTitle?: string
  articleSlug?: string
  userId: string
  userName?: string
  avatarUrl?: string
  parentId?: string
  replyToUserName?: string
  content: string
  status: string
  images?: string[]
  replies: CommentDTO[]
  createdAt: string
}

export interface CommentCreateDTO {
  articleId: string
  parentId?: string
  content: string
  images?: string[]
}

export interface ReactionResponseDTO {
  liked: boolean
  likeCount: number
}

export interface FavoriteResponseDTO {
  favorited: boolean
  favoriteCount: number
}

export interface UserArticleDTO {
  id: string
  articleId: string
  articleTitle?: string
  articleSlug?: string
  createdAt: string
}

export interface UserProfileDTO {
  userId: string
  displayName?: string
  bio?: string
  avatarUrl?: string
  gender?: string
}

export interface UpdateUserProfileDTO {
  displayName?: string
  avatarUrl?: string
  bio?: string
  gender?: string
}

export interface AboutDTO {
  content: string
  skills: string[]
  socialLinks: string
}

export interface UpdateAboutDTO {
  content?: string
  skills?: string[]
  socialLinks?: string
}

export interface AnnouncementDTO {
  title: string
  content: string
}

export interface UpdateAnnouncementDTO {
  title?: string
  content?: string
}

export interface GuestbookDTO {
  id: string
  userId: string
  nickname?: string
  avatarUrl?: string
  content: string
  status: string
  createdAt: string
}

export interface GuestbookCreateDTO {
  content: string
  nickname?: string
  avatarUrl?: string
}

export interface GuestLoginDTO {
  email: string
  code: string
}
