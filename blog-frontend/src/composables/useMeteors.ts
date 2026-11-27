import { onBeforeUnmount, watch, type Ref } from 'vue'

/**
 * 流星背景动画（Canvas 版）。
 *
 * 相比手写版本解决了几件事：
 * 1. 按 devicePixelRatio 放大画布再缩放绘制 —— 高分屏/浏览器缩放下不再是糊的；
 * 2. 速度按「像素/秒」计算，配合 dt —— 120Hz 屏幕上不会变成两倍速；
 * 3. 用叠加混合（lighter）+ 头部径向光晕画尾迹 —— 比 shadowBlur 便宜且更亮，
 *    shadowBlur 每颗每帧都要重新算模糊，30 颗就是 30 次；
 * 4. 离屏或切到别的标签页就停掉 requestAnimationFrame —— 滚到下面看文章时不再空转；
 * 5. 尊重 prefers-reduced-motion，用户开了「减少动态效果」就不画。
 *
 * 用法：
 * ```ts
 * const canvasRef = ref<HTMLCanvasElement | null>(null)
 * useMeteors(canvasRef)
 * ```
 * 画布尺寸取父元素的实际显示尺寸，所以 `position: fixed` 满屏和只占 hero 一条都能用。
 */

interface Meteor {
  /** 头部位置（px，CSS 像素） */
  x: number
  y: number
  /** 前进方向与水平线的夹角（弧度），流星朝左下飞 */
  angle: number
  /** 像素/秒 */
  speed: number
  /** 尾迹长度（px） */
  length: number
  /** 头部线宽（px） */
  width: number
  /** 亮度基准 0~1 */
  brightness: number
  /** 已飞行时间与总寿命（秒） */
  age: number
  life: number
}

export interface UseMeteorsOptions {
  /** 每多少平方像素一颗流星，越小越密 */
  areaPerMeteor?: number
  maxCount?: number
  minCount?: number
  /** 与水平线的角度范围（度） */
  angleRange?: [number, number]
  /** 头部飞行速度范围（px/秒）。调小会更慢更写意 */
  speedRange?: [number, number]
  /** 头部颜色 RGB */
  headColor?: [number, number, number]
  /** 用户开启「减少动态效果」时是否停止绘制 */
  respectReducedMotion?: boolean
}

const DEFAULTS: Required<UseMeteorsOptions> = {
  areaPerMeteor: 52000,
  maxCount: 26,
  minCount: 7,
  angleRange: [26, 46],
  speedRange: [115, 330],
  headColor: [236, 246, 255],
  respectReducedMotion: true,
}

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value))
const rand = (min: number, max: number) => min + Math.random() * (max - min)

export function useMeteors(
  canvasRef: Ref<HTMLCanvasElement | null>,
  options: UseMeteorsOptions = {},
) {
  const opts = { ...DEFAULTS, ...options }
  const [r, g, b] = opts.headColor

  let canvas: HTMLCanvasElement | null = null
  let ctx: CanvasRenderingContext2D | null = null
  let meteors: Meteor[] = []
  let rafId = 0
  let running = false
  let lastTime = 0
  let cssWidth = 0
  let cssHeight = 0
  let dpr = 1

  let resizeObserver: ResizeObserver | null = null
  let intersectionObserver: IntersectionObserver | null = null
  let onScreen = true

  const prefersReducedMotion = () =>
    opts.respectReducedMotion &&
    typeof window !== 'undefined' &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches

  function createMeteor(): Meteor {
    const angle = (rand(opts.angleRange[0], opts.angleRange[1]) * Math.PI) / 180
    // depth 越大越"近"：更粗、更快、更亮
    const depth = Math.random() ** 1.6
    const [speedMin, speedMax] = opts.speedRange
    const speed = speedMin + (speedMax - speedMin) * depth
    const length = 70 + depth * 190
    // 流星朝左下飞，只会从「上边缘」或「右边缘」进入画面。
    // 只从上边缘进是不够的：宽屏（如 1248×808）要覆盖右下角 (W, H)，
    // 从上方出发得在 x = W + H/tanθ ≈ 2400 起跳，远在画面之外，
    // 所以那一角永远等不到流星。改成一半走上边缘、一半走右边缘，两条边都铺满。
    const fromTop = Math.random() < 0.5
    const startX = fromTop
      ? rand(-cssWidth * 0.08, cssWidth * 1.06)
      : rand(cssWidth * 0.9, cssWidth * 1.2)
    const startY = fromTop
      ? -rand(length * 0.6, cssHeight * 0.32)
      : rand(-cssHeight * 0.15, cssHeight * 1.02)

    return {
      x: startX,
      y: startY,
      angle,
      speed,
      length,
      width: 0.9 + depth * 1.5,
      brightness: 0.42 + depth * 0.58,
      age: 0,
      // 飞出画面大致需要的时间，再留一点富余
      life: (cssHeight / Math.sin(angle) + length + cssWidth * 0.25) / speed,
    }
  }

  function resetMeteors() {
    const count = clamp(
      Math.round((cssWidth * cssHeight) / opts.areaPerMeteor),
      opts.minCount,
      opts.maxCount,
    )
    meteors = Array.from({ length: count }, () => {
      const meteor = createMeteor()
      // 初始化时让每颗处于生命周期的不同阶段，避免一进页面全部同一起跑线
      meteor.age = Math.random() * meteor.life * 0.8
      return meteor
    })
  }

  function resize() {
    if (!canvas) return
    const parent = canvas.parentElement
    const rect = parent?.getBoundingClientRect()
    const width = Math.max(1, Math.round(rect?.width || window.innerWidth))
    const height = Math.max(1, Math.round(rect?.height || window.innerHeight))

    const nextDpr = clamp(window.devicePixelRatio || 1, 1, 2.5)
    // 尺寸没变就什么都不做，避免布局微抖动导致每帧重建
    if (width === cssWidth && height === cssHeight && nextDpr === dpr) return

    const prevWidth = cssWidth
    const prevHeight = cssHeight

    cssWidth = width
    cssHeight = height
    dpr = nextDpr
    canvas.width = Math.round(cssWidth * dpr)
    canvas.height = Math.round(cssHeight * dpr)
    // 显式把 CSS 盒子也定成同样的尺寸。
    // 样式表里写 100vw/100vh 时，100vw 含滚动条宽度，画布缓冲区和显示尺寸会差十几像素，
    // 浏览器再把缓冲区拉伸到 CSS 尺寸就糊了。这里内联覆盖掉，两边严格一致。
    canvas.style.width = `${cssWidth}px`
    canvas.style.height = `${cssHeight}px`

    ctx = canvas.getContext('2d')
    if (ctx) ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

    if (meteors.length === 0 || prevWidth === 0) {
      resetMeteors()
    } else {
      // 按比例缩放已有流星，窗口拉大拉小时不会突然"换一批"
      const sx = cssWidth / prevWidth
      const sy = cssHeight / prevHeight
      meteors.forEach(meteor => {
        meteor.x *= sx
        meteor.y *= sy
      })
    }
  }

  function step(now: number) {
    if (!running) return
    rafId = window.requestAnimationFrame(step)
    if (!ctx) return

    // 切标签页回来时 dt 会非常大，钳一下避免流星瞬移
    const dt = lastTime === 0 ? 0.016 : clamp((now - lastTime) / 1000, 0, 0.05)
    lastTime = now

    ctx.clearRect(0, 0, cssWidth, cssHeight)
    ctx.globalCompositeOperation = 'lighter'

    for (let i = 0; i < meteors.length; i++) {
      const meteor = meteors[i]
      meteor.age += dt

      const dx = Math.cos(meteor.angle)
      const dy = Math.sin(meteor.angle)
      meteor.x -= dx * meteor.speed * dt
      meteor.y += dy * meteor.speed * dt

      if (meteor.age >= meteor.life || meteor.x < -meteor.length || meteor.y > cssHeight + meteor.length) {
        meteors[i] = createMeteor()
        continue
      }

      const progress = clamp(meteor.age / meteor.life, 0, 1)
      const fadeIn = clamp(meteor.age / 0.35, 0, 1)
      // 越飞越暗，像在高空烧掉
      const alpha = fadeIn * (1 - progress * 0.65) * meteor.brightness
      if (alpha <= 0.005) continue

      const tailX = meteor.x + dx * meteor.length
      const tailY = meteor.y - dy * meteor.length

      // 尾迹：头部最亮，35% 处仍有微光，末端完全透明
      const tail = ctx.createLinearGradient(meteor.x, meteor.y, tailX, tailY)
      tail.addColorStop(0, `rgba(${r},${g},${b},${alpha})`)
      tail.addColorStop(0.3, `rgba(${r},${g},${b},${alpha * 0.36})`)
      tail.addColorStop(1, `rgba(${r},${g},${b},0)`)

      ctx.strokeStyle = tail
      ctx.lineWidth = meteor.width
      ctx.lineCap = 'round'
      ctx.beginPath()
      ctx.moveTo(meteor.x, meteor.y)
      ctx.lineTo(tailX, tailY)
      ctx.stroke()

      // 头部光晕：径向渐变一层就够，比 shadowBlur 便宜
      const glowRadius = meteor.width * 4.2
      const glow = ctx.createRadialGradient(
        meteor.x, meteor.y, 0,
        meteor.x, meteor.y, glowRadius,
      )
      glow.addColorStop(0, `rgba(${r},${g},${b},${alpha * 0.9})`)
      glow.addColorStop(0.4, `rgba(${r},${g},${b},${alpha * 0.22})`)
      glow.addColorStop(1, `rgba(${r},${g},${b},0)`)

      ctx.fillStyle = glow
      ctx.beginPath()
      ctx.arc(meteor.x, meteor.y, glowRadius, 0, Math.PI * 2)
      ctx.fill()
    }

    ctx.globalCompositeOperation = 'source-over'
  }

  function start() {
    if (running || !onScreen || document.hidden) return
    if (prefersReducedMotion()) {
      ctx?.clearRect(0, 0, cssWidth, cssHeight)
      return
    }
    running = true
    lastTime = 0
    rafId = window.requestAnimationFrame(step)
  }

  function stop() {
    running = false
    if (rafId) {
      window.cancelAnimationFrame(rafId)
      rafId = 0
    }
  }

  function handleVisibilityChange() {
    if (document.hidden) stop()
    else start()
  }

  function setup(element: HTMLCanvasElement) {
    canvas = element
    resize()
    start()

    if (typeof ResizeObserver !== 'undefined') {
      resizeObserver = new ResizeObserver(() => resize())
      resizeObserver.observe(element.parentElement ?? element)
    } else {
      window.addEventListener('resize', resize)
    }

    if (typeof IntersectionObserver !== 'undefined') {
      // 滚出视口就停，别在用户看正文时还空转
      intersectionObserver = new IntersectionObserver(
        entries => {
          onScreen = entries.some(entry => entry.isIntersecting)
          if (onScreen) start()
          else stop()
        },
        { threshold: 0 },
      )
      // 观察父元素而不是 canvas 自己：canvas 是 position:fixed 铺满视口的，
      // 它自己永远不会离开视口；真正该判断的是 hero 区块有没有滚走。
      intersectionObserver.observe(element.parentElement ?? element)
    }

    document.addEventListener('visibilitychange', handleVisibilityChange)
  }

  function teardown() {
    stop()
    resizeObserver?.disconnect()
    resizeObserver = null
    intersectionObserver?.disconnect()
    intersectionObserver = null
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    window.removeEventListener('resize', resize)
    meteors = []
    canvas = null
    ctx = null
  }

  watch(
    canvasRef,
    element => {
      teardown()
      if (element) setup(element)
    },
    { flush: 'post' },
  )

  onBeforeUnmount(teardown)
}
