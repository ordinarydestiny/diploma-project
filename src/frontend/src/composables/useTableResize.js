import { onMounted, onBeforeUnmount, nextTick } from 'vue'

const RESIZE_THRESHOLD = 8

export function useTableResize(tableRef) {
  let headerEl = null
  let mouseMoveHandler = null
  let mouseDownHandler = null

  function getLastVisibleTh(wrapper) {
    const ths = wrapper.querySelectorAll('th.el-table__cell:not(.is-hidden)')
    return ths.length > 0 ? ths[ths.length - 1] : null
  }

  function isNearRightBorder(e, th) {
    if (!th) return false
    const rect = th.getBoundingClientRect()
    return e.clientX > rect.right - RESIZE_THRESHOLD && e.clientX <= rect.right
  }

  function setup() {
    const tableEl = tableRef.value?.$el
    if (!tableEl) return

    headerEl = tableEl.querySelector('.el-table__header-wrapper')
    if (!headerEl) return

    mouseMoveHandler = (e) => {
      const lastTh = getLastVisibleTh(headerEl)
      if (isNearRightBorder(e, lastTh)) {
        document.body.style.cursor = ''
        lastTh.style.cursor = ''
      }
    }

    mouseDownHandler = (e) => {
      const lastTh = getLastVisibleTh(headerEl)
      if (isNearRightBorder(e, lastTh)) {
        e.stopPropagation()
      }
    }

    headerEl.addEventListener('mousemove', mouseMoveHandler, true)
    headerEl.addEventListener('mousedown', mouseDownHandler, true)
  }

  function cleanup() {
    if (headerEl) {
      if (mouseMoveHandler) headerEl.removeEventListener('mousemove', mouseMoveHandler, true)
      if (mouseDownHandler) headerEl.removeEventListener('mousedown', mouseDownHandler, true)
    }
    headerEl = null
    mouseMoveHandler = null
    mouseDownHandler = null
  }

  onMounted(async () => {
    await nextTick()
    setup()
  })

  onBeforeUnmount(() => {
    cleanup()
  })
}
