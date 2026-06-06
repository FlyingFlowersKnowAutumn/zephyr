import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/login-form',
      name: 'LoginForm',
      component: () => import('@/views/LoginFormView.vue')
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/HomeView.vue')
    },
    {
      path: '/write',
      name: 'Write',
      component: () => import('@/views/WriteView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/articles',
      name: 'Articles',
      component: () => import('@/views/ArticlesView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/articles/:slug',
      name: 'ArticleDetail',
      component: () => import('@/views/ArticleDetailView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/articles/:slug/edit',
      name: 'ArticleEdit',
      component: () => import('@/views/WriteView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/search',
      name: 'Search',
      component: () => import('@/views/SearchView.vue')
    },
    {
      path: '/questions',
      name: 'Questions',
      component: () => import('@/views/QuestionsView.vue')
    },
    {
      path: '/questions/ask',
      name: 'AskQuestion',
      component: () => import('@/views/AskQuestionView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/questions/:id',
      name: 'QuestionDetail',
      component: () => import('@/views/QuestionDetailView.vue')
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/:userId',
      name: 'UserProfile',
      component: () => import('@/views/ProfileView.vue')
    },
    {
      path: '/about',
      name: 'About',
      component: () => import('@/views/AboutView.vue')
    },
    {
      path: '/guestbook',
      name: 'Guestbook',
      component: () => import('@/views/GuestbookView.vue')
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
    return
  }
  
  if (to.meta.requiredRole && authStore.user?.role !== to.meta.requiredRole) {
    next('/')
    return
  }
  
  if ((to.path === '/login' || to.path === '/login-form') && authStore.isLoggedIn) {
    next('/')
    return
  }
  
  next()
})

export default router
