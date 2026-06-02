<template>
  <div class="page-container">
    <el-card>
      <template #header><span class="card-title">毕业设计通知</span></template>

      <el-table ref="tableRef" :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="内容摘要" min-width="300" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布人" min-width="100" />
        <el-table-column prop="publishTime" label="发布时间" min-width="170" />
        <el-table-column prop="deadline" label="截止日期" min-width="170" />
        <el-table-column label="操作" width="150" :resizable="false">
          <template #default="{ row }"><el-button link type="primary" size="small" @click="handleView(row)">查看</el-button></template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total"
          :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData"/>
      </div>
    </el-card>

    <el-dialog v-model="viewVisible" title="毕业设计通知详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ viewData.publisherName||'-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ viewData.publishTime||'-' }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ viewData.deadline||'-' }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2" class="notice-content">{{ viewData.content||'-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request.js'
import { useTableResize } from '@/composables/useTableResize.js'

const tableRef = ref(null)
useTableResize(tableRef)
const loading = ref(false), tableData = ref([]), viewVisible = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })
const viewData = ref({})

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/graduation/notices', { params: { current: pagination.current, size: pagination.size } })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e.message) } finally { loading.value = false }
}

function handleView(row) { viewData.value={...row}; viewVisible.value=true }
onMounted(fetchData)
</script>

<style scoped>
.page-container{padding:20px}.card-title{font-size:16px;font-weight:600}.pagination-wrapper{margin-top:16px;display:flex;justify-content:flex-end}.notice-content{white-space:pre-wrap;line-height:1.6}
</style>
