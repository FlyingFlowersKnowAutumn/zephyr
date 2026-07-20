import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { incrementViewCount } from '@/api/site'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.path === '/') return { top: 0 }
    return { top: 0 }
  },
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue')
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
      meta: { requiresAuth: true, requiredRole: 'blogger' }
    },
    {
      path: '/articles',
      name: 'Articles',
      component: () => import('@/views/ArticlesView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/articles/manage',
      name: 'ArticlesManage',
      component: () => import('@/views/ArticlesManageView.vue'),
      meta: { requiresAuth: true, requiredRole: 'blogger' }
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
      meta: { requiresAuth: true, requiredRole: 'blogger' }
    },
    {
      path: '/search',
      name: 'Search',
      component: () => import('@/views/SearchView.vue')
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
    },
    {
      path: '/archive',
      name: 'Archive',
      component: () => import('@/views/ArchiveView.vue')
    },
    {
      path: '/categories',
      name: 'Categories',
      component: () => import('@/views/CategoryView.vue')
    },
    {
      path: '/tags',
      name: 'Tags',
      component: () => import('@/views/TagView.vue')
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
  
  if (to.path === '/login' && authStore.isLoggedIn) {
    next('/')
    return
  }
  
  next()
})

router.afterEach(() => {
  incrementViewCount().catch(() => {})
})

export default router
