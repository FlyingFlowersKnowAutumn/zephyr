export interface UserDTO {
  id: string
  username: string
  displayName?: string
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
  createdAt: string
}

export interface CommentDTO {
  id: string
  articleId: string
  userId: string
  userName?: string
  parentId?: string
  content: string
  status: string
  replies: CommentDTO[]
  createdAt: string
}

export interface CommentCreateDTO {
  articleId: string
  parentId?: string
  content: string
}

export interface ReactionResponseDTO {
  liked: boolean
  likeCount: number
}

export interface QuestionDTO {
  id: string
  userId: string
  userName?: string
  title: string
  content: string
  status: string
  isPublic: boolean
  answers: AnswerDTO[]
  createdAt: string
  updatedAt: string
}

export interface QuestionCreateDTO {
  title: string
  content: string
  isPublic: boolean
}

export interface AnswerDTO {
  id: string
  questionId: string
  userId: string
  userName?: string
  content: string
  isAccepted: boolean
  status: string
  createdAt: string
}

export interface UserProfileDTO {
  userId: string
  displayName?: string
  bio?: string
  avatarUrl?: string
  website?: string
  github?: string
  twitter?: string
  about?: string
}

export interface UpdateUserProfileDTO {
  displayName?: string
  bio?: string
  avatarUrl?: string
  website?: string
  github?: string
  twitter?: string
  about?: string
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
