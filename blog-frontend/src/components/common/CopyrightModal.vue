<script setup lang="ts">
import { watch } from 'vue'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ (e: 'close'): void }>()

function handleClose() {
  emit('close')
}

function handleOverlayClick(e: MouseEvent) {
  if ((e.target as HTMLElement).classList.contains('modal-overlay')) {
    handleClose()
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
        <div class="modal-container">
          <button class="close-btn" @click="handleClose">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
          
          <div class="modal-header">
            <h3>版权声明</h3>
          </div>
          
          <div class="modal-body">
            <p class="copyright-intro">
              Zéphyr·Journal 是运行在 zephyr-journal.com 域名及相关子域名上的个人技术博客网站，本条款描述了本站的网站版权声明：
            </p>
            
            <ul class="copyright-list">
              <li>
                Zéphyr·Journal 提供的所有文章、展示的图片素材等内容部分来源于互联网平台，仅供大家学习参考，如有侵犯您的版权，请联系博主，我们将在一个工作日内改正。
              </li>
              <li>
                Zéphyr·Journal 不保证网站内容的全部准确性、安全性和完整性，请您在阅读、下载及使用过程中自行确认，本站亦不承担上述资源对您造成的任何形式的损失或伤害。
              </li>
              <li>
                未经本站允许，不得盗链、盗用本站内容和资源。
              </li>
              <li>
                Zéphyr·Journal 旨在为广大用户提供更多的信息；本站不保证向用户提供的外部链接的准确性和完整性，该外部链接指向的不由本站实际控制的任何网页上的内容，本站对其合法性亦概不负责，亦不承担任何法律责任。
              </li>
              <li>
                本站中的文章/视频（包括转载文章/视频）的版权仅归原作者所有，若作者有版权声明或文章从其他网站转载而附带有原所有站的版权声明者，其版权归属以附带声明为准；文章仅代表作者本人的观点，与本站立场无关。
              </li>
              <li>
                Zéphyr·Journal 自行编写排版的文章均采用 <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/" target="_blank" rel="noopener">知识共享署名-非商业性使用-相同方式共享 4.0 国际许可协议</a>。
              </li>
              <li>许可协议标识：<img src="https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" alt="知识共享许可协议" /></li>
            </ul>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 1rem;
}

.modal-container {
  background: #fff;
  border-radius: 8px;
  max-width: 800px;
  width: 100%;
  max-height: 70vh;
  overflow: hidden;
  position: relative;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  z-index: 10;

  &:hover {
    background: rgba(239, 68, 68, 0.1);
    border-color: #ef4444;
    color: #ef4444;
  }
}

.modal-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #eee;

  h3 {
    margin: 0;
    font-size: 1.1rem;
    font-weight: 600;
    color: #333;
    text-align: center;
  }
}

.modal-body {
  padding: 1.25rem 1.5rem;
  overflow-y: auto;
  max-height: calc(70vh - 80px);
}

.copyright-intro {
  font-size: 0.85rem;
  color: #666;
  line-height: 1.6;
  margin-bottom: 1rem;
  text-align: justify;
}

.copyright-list {
  list-style: disc;
  padding-left: 1.25rem;
  margin: 0;

  li {
    margin-bottom: 0.5rem;
    font-size: 0.85rem;
    color: #666;
    line-height: 1.6;
    text-align: justify;

    &:last-child {
      margin-bottom: 0;
    }

    img {
      vertical-align: middle;
      margin-left: 0.25rem;
    }

    a {
      color: #007bff;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;

  .modal-container {
    transform: scale(0.95) translateY(20px);
  }
}

@media (max-width: 768px) {
  .modal-overlay {
    padding: 0.5rem;
  }

  .modal-body {
    padding: 1rem;
  }

  .license-badge {
    flex-direction: column;
    text-align: center;
  }
}
</style>
