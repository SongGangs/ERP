/**
 * TabLinkMixin - 用于在新 Tab 中打开页面并传递参数
 *
 * 使用方法：
 * 1. 在组件中引入：import { TabLinkMixin } from '@/mixins/TabLinkMixin'
 * 2. 在 mixins 中添加：mixins: [TabLinkMixin]
 * 3. 调用方法：this.openInNewTab(path, query, title, componentName)
 *
 * 示例：
 * this.openInNewTab('/financial/item_in', { accountId: 1, beginDate: '2025-01-01' }, '收入单', 'ItemInList')
 */

export const TabLinkMixin = {
  methods: {
    /**
     * 在新 Tab 中打开页面
     * @param {String} path - 目标路径，如 '/financial/item_in'
     * @param {Object} query - 查询参数对象，如 { accountId: 1, beginDate: '2025-01-01' }
     * @param {String} title - Tab 标题，如 '收入单'
     * @param {String} componentName - 组件名称，如 'ItemInList'
     */
    openInNewTab(path, query = {}, title = '新页面', componentName = '') {
      // 查找 TabLayout 组件
      let tabLayout = this.$parent
      while (tabLayout && tabLayout.$options.name !== 'TabLayout') {
        tabLayout = tabLayout.$parent
      }

      if (tabLayout) {
        // 检查该 tab 是否已存在（只检查 path，不检查参数）
        const existingIndex = tabLayout.linkList.indexOf(path)
        if (existingIndex >= 0) {
          // 如果已存在，直接切换到该 tab，并通过路由传递参数
          this.$router.push({
            path: path,
            query: query
          }).then(() => {
            this.$nextTick(() => {
              tabLayout.activePage = path
            })
          }).catch(err => {
            if (err.name !== 'NavigationDuplicated') {
              console.error('TabLinkMixin - 路由跳转失败:', err)
            }
          })
        } else {
          // 如果不存在，手动添加到 pageList 和 linkList（不带参数）
          tabLayout.pageList.push({
            name: title,
            path: path,
            fullPath: path,  // 注意：这里不带参数
            query: {},       // 不保存参数到 pageList
            meta: {
              title: title,
              componentName: componentName,
              keepAlive: true
            }
          })
          tabLayout.linkList.push(path)  // 注意：这里不带参数

          // 使用 router.push 跳转（带参数）
          this.$router.push({
            path: path,
            query: query
          }).then(() => {
            // 路由跳转完成后，激活新 tab
            this.$nextTick(() => {
              tabLayout.activePage = path  // 注意：这里不带参数
            })
          }).catch(err => {
            // 忽略导航重复错误
            if (err.name !== 'NavigationDuplicated') {
              console.error('TabLinkMixin - 路由跳转失败:', err)
            }
          })
        }
      } else {
        // 如果找不到 TabLayout，使用普通的 router.push
        this.$router.push({
          path: path,
          query: query
        }).catch(err => {
          if (err.name !== 'NavigationDuplicated') {
            console.error('TabLinkMixin - 路由跳转失败:', err)
          }
        })
      }
    }
  }
}

