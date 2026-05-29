/**
 * 列表管理组合式函数模块
 * 
 * 功能说明：
 * 1. 封装列表数据管理的通用逻辑
 * 2. 提供分页、搜索、加载状态管理
 * 3. 支持自定义查询参数
 * 
 * 使用规范：
 * - 在需要列表管理的组件中使用
 * - 传入获取数据的 API 函数
 * - 返回的状态和方法可直接在模板中使用
 * 
 * 示例：
 * const { list, total, pagination, searchForm, loading, loadList } = useList(getListApi)
 */
// 导入Vue响应式API
import { ref, reactive } from 'vue'

/**
 * 列表管理组合式函数
 * @param {Function} fetchApi - 获取数据的API函数
 * @returns {Object} 列表相关的状态和方法
 */
export function useList(fetchApi) {
  // 列表数据数组
  const list = ref([])
  // 数据总数
  const total = ref(0)
  // 分页参数配置
  const pagination = reactive({
    pageNum: 1,       // 当前页码
    pageSize: 10      // 每页显示条数
  })
  // 搜索表单数据
  const searchForm = reactive({})
  // 加载状态标识
  const loading = ref(false)
  // 错误状态标识
  const hasError = ref(false)

  /**
   * 加载列表数据
   * @param {Object} customParams - 自定义查询参数
   */
/*
  传统函数写法：
  const loadList = function(customParams) {
    if (customParams === undefined) {
      customParams = {}
    }
  // ... 函数体
  }
  箭头函数简化版：
  // 第一步：用箭头替换 function
  const loadList = (customParams) => {
  // ... 函数体
  }

// 第二步：参数默认值简化
  const loadList = (customParams = {}) => {
  // ... 函数体
  }

  // 第三步：如果只有一个语句，可以省略花括号和 return
  const double = (x) => x * 2
  // 等同于
  const double = (x) => {
    return x * 2
  }  */
  
  const loadList = async (customParams = {}) => {
    // 设置加载状态为true
    loading.value = true
    // 重置错误状态
    hasError.value = false

    try {
      // 合并分页参数、搜索表单和自定义参数
      const params = {
        ...pagination, // 分页参数：{ pageNum: 1, pageSize: 10 }
        ...searchForm, // 搜索表单参数：比如 { name: '张三' }
        ...customParams // 自定义参数：调用时额外传的
      }

      // 调用传入的API函数获取数据
      const response = await fetchApi(params)
      
      // 判断请求是否成功
      if (response.code === 200) {
        const data = response.data
        //{records: [{},{},{},{}], total: 14,size: 10,"current": 2,pages: 2}
        //current:代表当前页码 pages:代表总页数
        //total:代表总条数 size:代表每页条数
        
        // 提取列表数据，支持多种数据结构
        list.value = data.records || data || []
        // 设置数据总数
        total.value = data.total || list.value.length
      }
    } catch (error) {
      hasError.value = true
      // axios拦截器已统一处理错误提示
    } finally {
      // 无论成功或失败，都关闭加载状态
      loading.value = false
    }
  }

  /**
   * 执行搜索
   * @param {Object} form - 搜索条件表单数据
   */
  const search = (form = {}) => {
    // 更新搜索表单数据
    Object.assign(searchForm, form)
    // 重置到第一页
    pagination.pageNum = 1
    // 重新加载数据
    loadList()
  }

  /**
   * 重置搜索条件
   * 清空所有搜索字段并重新加载数据
   */
  const resetSearch = () => {
    // 遍历搜索表单，清空所有字段
    Object.keys(searchForm).forEach(key => {
      searchForm[key] = ''
    })
    // 重置到第一页
    pagination.pageNum = 1
    // 重新加载数据
    loadList()
  }

  /**
   * 处理分页变化
   * @param {Object} param0 - 分页参数对象
   * @param {number} param0.pageNum - 新的页码
   * @param {number} param0.pageSize - 新的每页条数
   */
  const handlePageChange = ({ pageNum, pageSize }) => {
    // 更新分页参数
    pagination.pageNum = pageNum
    pagination.pageSize = pageSize
    // 根据新参数加载数据
    loadList()
  }

  /**
   * 刷新列表数据
   * 保持当前分页和搜索条件不变
   */
  const refresh = () => {
    loadList()
  }

  // 返回所有状态和方法供组件使用
  return {
    list,           // 列表数据
    total,          // 数据总数
    pagination,     // 分页配置
    searchForm,     // 搜索表单
    loading,        // 加载状态
    hasError,       // 错误状态
    loadList,       // 加载数据方法
    search,         // 搜索方法
    resetSearch,    // 重置搜索方法
    handlePageChange, // 分页处理方法
    refresh         // 刷新方法
  }
}
/* 闭包的本质
function createCounter() {
  let count = 0; // 这是一个局部变量
  
  // 这是一个内部函数
  function increment() {
    count++; // 它引用了外面的 count
    console.log(count);
  }
  
  return increment; // 把这个内部函数送出去
}

const myCounter = createCounter(); // 此时 createCounter 执行完了
myCounter(); // 输出 1
myCounter(); // 输出 2
神奇的地方在于： 虽然 createCounter 已经跑完了，但 count 依然活着！因为 increment 函数还抓着它不放。 */
/* useList 里也是一样的：
loadList 抓着 list 和 pagination。
search 抓着 searchForm 和 pagination。
只要你还在使用这些返回的方法，那些状态就会一直存在。 */
