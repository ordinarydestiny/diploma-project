<template>
  <div class="home-container">
    <div class="search-section">
      <el-row :gutter="16">
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
          :lg="5"
        >
          <div class="search-item">
            <label>实习计划</label>
            <el-select
              v-model="searchForm.plan"
              placeholder="请选择实习计划"
              clearable
              style="width: 100%"
            >
              <el-option
                label="2018级软件技术专业顶岗实习"
                value="1"
              />
              <el-option
                label="2019级软件技术专业顶岗实习"
                value="2"
              />
              <el-option
                label="2020级软件技术专业顶岗实习"
                value="3"
              />
            </el-select>
          </div>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
          :lg="5"
        >
          <div class="search-item">
            <label>指导老师</label>
            <el-input
              v-model="searchForm.teacher"
              placeholder="请输入指导老师"
              clearable
            />
          </div>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
          :lg="5"
        >
          <div class="search-item">
            <label>实习学生</label>
            <el-input
              v-model="searchForm.student"
              placeholder="请输入实习学生"
              clearable
            />
          </div>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
          :lg="4"
        >
          <div class="search-item">
            <label>是否免实习</label>
            <el-select
              v-model="searchForm.exempt"
              placeholder="请选择是否免实习"
              clearable
              style="width: 100%"
            >
              <el-option
                label="是"
                value="yes"
              />
              <el-option
                label="否"
                value="no"
              />
            </el-select>
          </div>
        </el-col>
        <el-col
          :xs="24"
          :sm="24"
          :md="24"
          :lg="5"
        >
          <div class="search-buttons">
            <el-button
              type="primary"
              @click="handleSearch"
            >
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="table-section">
      <div class="table-header">
        <el-button
          type="warning"
          @click="handleExport"
        >
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <div class="table-actions">
          <el-button
            circle
            @click="handleRefresh"
          >
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column
          type="selection"
          width="55"
          align="center"
        />
        <el-table-column
          prop="plan"
          label="实习计划"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="studentId"
          label="学生学号"
          width="120"
          align="center"
        />
        <el-table-column
          prop="studentName"
          label="学生姓名"
          width="100"
          align="center"
        />
        <el-table-column
          prop="studentPhone"
          label="学生电话"
          width="130"
          align="center"
        />
        <el-table-column
          prop="companyName"
          label="单位名称"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column
          prop="companyTeacherName"
          label="单位指导老师姓名"
          width="140"
          align="center"
        />
        <el-table-column
          prop="companyTeacherPhone"
          label="单位指导老师联系电话"
          width="160"
          align="center"
        />
        <el-table-column
          prop="startDate"
          label="实习开始时间"
          width="120"
          align="center"
        />
        <el-table-column
          prop="endDate"
          label="实习结束时间"
          width="120"
          align="center"
        />
        <el-table-column
          prop="teacher"
          label="指导老师"
          width="100"
          align="center"
        />
        <el-table-column
          prop="isExempt"
          label="是否免实习"
          width="110"
          align="center"
        >
          <template #default="{ row }">
            <el-switch
              v-model="row.isExempt"
              active-text="是"
              inactive-text="否"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="120"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleView(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-section">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @update:page-size="pageSize = $event"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

/**
 *
 */
const loading = ref(false)
/**
 *
 */
const currentPage = ref(1)
/**
 *
 */
const pageSize = ref(10)
/**
 *
 */
const total = ref(1)

/**
 *
 */
const searchForm = reactive({
  plan: '',
  teacher: '',
  student: '',
  exempt: ''
})

/**
 *
 */
const tableData = ref([
  {
    id: 1,
    plan: '2018级软件技术专业顶岗实习',
    studentId: '1831613107',
    studentName: '游姚',
    studentPhone: '18223534250',
    companyName: '重庆市江津区野然电子商务服务部',
    companyTeacherName: '陈纲',
    companyTeacherPhone: '13251314586',
    startDate: '2020-12-03',
    endDate: '2021-06-01',
    teacher: '谢先伟',
    isExempt: false
  }
])

/**
 *
 */
const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('搜索完成')
  }, 500)
}

/**
 *
 */
const handleReset = () => {
  searchForm.plan = ''
  searchForm.teacher = ''
  searchForm.student = ''
  searchForm.exempt = ''
  ElMessage.info('已重置搜索条件')
}

/**
 *
 */
const handleExport = () => {
  ElMessage.success('导出成功')
}

/**
 *
 */
const handleRefresh = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

/**
 *
 * @param row
 */
const handleView = (row) => {
  ElMessage.info(`查看 ${row.studentName} 的详情`)
}

/**
 *
 * @param val
 */
const handleSizeChange = (val) => {
  pageSize.value = val
  handleSearch()
}

/**
 *
 * @param val
 */
const handleCurrentChange = (val) => {
  currentPage.value = val
  handleSearch()
}
</script>

<style scoped>
.home-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-item {
  margin-bottom: 10px;
}

.search-item label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.search-buttons {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  height: 100%;
  padding-top: 28px;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  gap: 10px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .search-buttons {
    padding-top: 10px;
  }
  
  .table-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>
