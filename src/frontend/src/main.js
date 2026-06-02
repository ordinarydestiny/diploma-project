/**
 * 应用入口文件
 * 初始化Vue3应用，注册全局插件和组件
 * - ElementPlus：UI组件库（中文语言包）
 * - Pinia：状态管理
 * - VueRouter：路由管理
 * - ElementPlus Icons：全局注册所有图标组件
 */
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn.js'

dayjs.locale('zh-cn')

const customLocale = {
  ...zhCn,
  el: {
    ...zhCn.el,
    datepicker: {
      ...(zhCn.el?.datepicker || {}),
      firstDayOfWeek: 1
    }
  }
}
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './styles/index.css'

/** Vue应用实例 */
const app = createApp(App)

/** 全局注册Element Plus图标组件，可在模板中直接使用图标名 */
for (/** 遍历注册图标组件 */
const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: customLocale })
app.mount('#app')
