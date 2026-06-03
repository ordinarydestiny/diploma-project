<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>学生选题</h2>
          <el-button type="primary" @click="handleSelect">
            <el-icon><Check /></el-icon>
            选择题目
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="可选题目" name="available">
          <el-table :data="availableTopics" border stripe v-loading="loading">
            <el-table-column prop="topicName" label="题目名称" min-width="250" show-overflow-tooltip />
            <el-table-column prop="teacherName" label="指导教师" width="120" align="center" />
            <el-table-column prop="type" label="类型" width="100" align="center">
              <template #default="{ row }">
                {{ row.type === 'design' ? '设计' : '论文' }}
              </template>
            </el-table-column>
            <el-table-column prop="selectedCount" label="已选/上限" width="120" align="center">
              <template #default="{ row }">
                {{ row.selectedCount }} / {{ row.maxStudents }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :disabled="row.selectedCount >= row.maxStudents" @click="handleChoose(row)">
                  选择
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的选题" name="my-selection">
          <el-empty v-if="!mySelection" description="尚未选择题目">
            <el-button type="primary" @click="activeTab = 'available'">去选择题目</el-button>
          </el-empty>
          <el-descriptions v-else :column="2" border>
            <el-descriptions-item label="题目名称">{{ mySelection.topicName }}</el-descriptions-item>
            <el-descriptions-item label="指导教师">{{ mySelection.teacherName }}</el-descriptions-item>
            <el-descriptions-item label="选题时间">{{ mySelection.selectedTime }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag type="success">已确认</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Check } from '@element-plus/icons-vue'

const loading = ref(false)
const activeTab = ref('available')

const availableTopics = ref([])
const mySelection = ref(null)

function handleSelect() {
  activeTab.value = 'available'
}

function handleChoose(row) {
  console.log('选择题目', row)
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
</style>
