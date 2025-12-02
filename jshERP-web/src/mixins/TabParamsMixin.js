/**
 * TabParamsMixin - 用于接收并应用 URL 参数
 *
 * 使用方法：
 * 1. 在目标页面组件中引入：import { TabParamsMixin } from '@/mixins/TabParamsMixin'
 * 2. 在 mixins 中添加：mixins: [TabParamsMixin]
 * 3. 实现 applyRouteParams 方法来处理参数
 *
 * 示例：
 * methods: {
 *   applyRouteParams(params) {
 *     if (params.accountId) {
 *       this.queryParam.accountId = parseInt(params.accountId)
 *     }
 *     if (params.beginDate && params.endDate) {
 *       this.queryParam.beginTime = params.beginDate
 *       this.queryParam.endTime = params.endDate
 *     }
 *     // 应用参数后重新加载数据
 *     this.loadData(1)
 *   }
 * }
 */

export const TabParamsMixin = {
  data() {
    return {
      // 是否清除 URL 参数（默认清除）
      clearUrlParams: true,
      // 清除参数的延迟时间（毫秒）
      clearParamsDelay: 500
    }
  },
  mounted() {
    // 从 URL 参数中获取并应用参数
    this._handleRouteParams()
  },
  watch: {
    // 监听路由变化，当路由参数更新时重新应用
    '$route.query': {
      handler(newQuery, oldQuery) {
        // 只有当参数真正变化时才重新应用
        if (newQuery && Object.keys(newQuery).length > 0) {
          const hasChanged = JSON.stringify(newQuery) !== JSON.stringify(oldQuery)
          if (hasChanged) {
            this._handleRouteParams()
          }
        }
      },
      deep: true
    }
  },
  methods: {
    /**
     * 处理路由参数（内部方法）
     * @private
     */
    _handleRouteParams() {
      const params = this.$route.query

      if (params && Object.keys(params).length > 0) {
        // 调用子组件实现的 applyRouteParams 方法
        if (typeof this.applyRouteParams === 'function') {
          this.applyRouteParams(params)

          // 如果需要清除 URL 参数
          if (this.clearUrlParams) {
            setTimeout(() => {
              this._clearUrlParams()
            }, this.clearParamsDelay)
          }
        } else {
          console.warn('TabParamsMixin: 请实现 applyRouteParams 方法来处理路由参数')
        }
      }
    },

    /**
     * 清除 URL 参数（内部方法）
     * @private
     */
    _clearUrlParams() {
      // 使用 history.replaceState 直接修改 URL，不触发路由变化
      const newUrl = window.location.origin + window.location.pathname
      window.history.replaceState({}, '', newUrl)

      // 由于 TabLinkMixin 已经使用不带参数的 path 创建 tab，所以这里不需要更新 TabLayout
    },

    /**
     * 应用路由参数（需要在使用的组件中实现）
     * @param {Object} params - 路由参数对象
     */
    applyRouteParams(params) {
      // 子组件需要实现此方法
      console.warn('TabParamsMixin: applyRouteParams 方法未实现')
    }
  }
}

