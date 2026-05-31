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
      meta: { requiresAuth: true, requiredRole: 'blogger' }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
    return
  }
  
  if (to.meta.requiredRole && authStore.user?.role !== to.meta.requiredRole) {
    next('/')
    return
  }
  
  next()
})

export default router
