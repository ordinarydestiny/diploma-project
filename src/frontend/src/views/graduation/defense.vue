<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>答辩管理</h2>
          <div class="header-actions">
            <el-select v-model="filterBatch" placeholder="选择批次" clearable style="width: 200px; margin-right: 10px" />
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="待答辩" name="pending">
          <el-table :data="pendingList" border stripe v-loading="loading">
            <el-table-column prop="studentName" label="学生姓名" width="120" align="center" />
            <el-table-column prop="studentId" label="学号" width="140" align="center" />
            <el-table-column prop="topicName" label="题目名称" min-width="250" show-overflow-tooltip />
            <el-table-column prop="teacherName" label="指导教师" width="120" align="center" />
            <el-table-column prop="defenseTime" label="答辩时间" width="160" align="center" />
            <el-table-column prop="defenseLocation" label="答辩地点" width="140" align="center" />
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleScore(row)">录入成绩</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已完成" name="completed">
          <el-table :data="completedList" border stripe v-loading="loading">
            <el-table-column prop="studentName" label="学生姓名" width="120" align="center" />
            <el-table-column prop="studentId" label="学号" width="140" align="center" />
            <el-table-column prop="topicName" label="题目名称" min-width="250" show-overflow-tooltip />
            <el-table-column prop="defenseScore" label="答辩成绩" width="100" align="center">
              <template #default="{ row }">
                <span :class="getScoreClass(row.defenseScore)">{{ row.defenseScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="finalScore" label="总成绩" width="100" align="center">
              <template #default="{ row }">
                <strong>{{ row.finalScore }}</strong>
              </template>
            </el-table-column>
            <el-table-column prop="gradeLevel" label="等级" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getGradeType(row.gradeLevel)">{{ row.gradeLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const loading = ref(false)
const activeTab = ref('pending')
const filterBatch = ref('')

const pendingList = ref([])
const completedList = ref([])

function getScoreClass(score) {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-medium'
  return 'score-pass'
}

function getGradeType(grade) {
  const map = { excellent: 'success', good: '', medium: 'warning', pass: 'info', fail: 'danger' }
  return map[grade] || ''
}

function handleSearch() {
  console.log('查询')
}

function handleScore(row) {
  console.log('录入成绩', row)
}

function handleView(row) {
  console.log('查看详情', row)
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
}

.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #409eff;
  font-weight: bold;
}

.score-medium {
  color: #e6a23c;
}

.score-pass {
  color: #909399;
}
</style>
