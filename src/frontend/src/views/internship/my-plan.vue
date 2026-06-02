<template>
  <div class="page-container">
    <el-card v-if="plan">
      <template #header>
        <span>我的实习计划</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划名称">
          {{ plan.planName }}
        </el-descriptions-item>
        <el-descriptions-item label="学期">
          {{ plan.semesterName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="专业">
          {{ plan.majorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="年级">
          {{ plan.gradeName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实习类型">
          {{ plan.internshipType || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="开始日期">
          {{ formatDate(plan.startDate) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="结束日期">
          {{ formatDate(plan.endDate) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="是否购买保险">
          {{ plan.hasInsurance === 1 ? '是' : '否' }}
        </el-descriptions-item>
        <el-descriptions-item label="保险费用">
          {{ plan.hasInsurance === 1 ? (plan.insuranceFee != null ? plan.insuranceFee + ' 元' : '-') : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实习补贴">
          {{ plan.subsidy != null ? plan.subsidy + ' 元' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="校内指导教师">
          {{ plan.innerTeacherName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="成绩占比说明" :span="2">
          <div class="weight-list">
            <el-tag v-for="item in weightItems" :key="item.label" class="weight-tag">
              {{ item.label }}：{{ item.value }}%
            </el-tag>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="plan" style="margin-top: 16px">
      <template #header>
        <span>我的实习成绩</span>
      </template>
      <div v-if="scoreData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="签到">
            <el-tag v-if="scoreData.checkinGrade" :type="gradeType(scoreData.checkinGrade)">{{ gradeText(scoreData.checkinGrade) }}</el-tag>
            <span v-else style="color: #c0c4cc">暂无</span>
          </el-descriptions-item>
          <el-descriptions-item label="日报">
            <el-tag v-if="scoreData.dailyReportGrade" :type="gradeType(scoreData.dailyReportGrade)">{{ gradeText(scoreData.dailyReportGrade) }}</el-tag>
            <span v-else style="color: #c0c4cc">暂无</span>
          </el-descriptions-item>
          <el-descriptions-item label="周报">
            <el-tag v-if="scoreData.weeklyReportGrade" :type="gradeType(scoreData.weeklyReportGrade)">{{ gradeText(scoreData.weeklyReportGrade) }}</el-tag>
            <span v-else style="color: #c0c4cc">暂无</span>
          </el-descriptions-item>
          <el-descriptions-item label="月报">
            <el-tag v-if="scoreData.monthlyReportGrade" :type="gradeType(scoreData.monthlyReportGrade)">{{ gradeText(scoreData.monthlyReportGrade) }}</el-tag>
            <span v-else style="color: #c0c4cc">暂无</span>
          </el-descriptions-item>
          <el-descriptions-item label="校内考核">
            <el-tag v-if="scoreData.innerAssessmentGrade" :type="gradeType(scoreData.innerAssessmentGrade)">{{ gradeText(scoreData.innerAssessmentGrade) }}</el-tag>
            <span v-else style="color: #c0c4cc">暂无</span>
          </el-descriptions-item>
          <el-descriptions-item label="校外考核">
            <el-tag v-if="scoreData.outerAssessmentGrade" :type="gradeType(scoreData.outerAssessmentGrade)">{{ gradeText(scoreData.outerAssessmentGrade) }}</el-tag>
            <span v-else style="color: #c0c4cc">暂无</span>
          </el-descriptions-item>
          <el-descriptions-item label="实习报告">
            <el-tag v-if="scoreData.reportGrade" :type="gradeType(scoreData.reportGrade)">{{ gradeText(scoreData.reportGrade) }}</el-tag>
            <span v-else style="color: #c0c4cc">暂无</span>
          </el-descriptions-item>
          <el-descriptions-item label="总成绩">
            <span v-if="scoreData.totalScore != null" style="font-size: 18px; font-weight: bold; color: #409eff">{{ scoreData.totalScore }} 分</span>
            <span v-else style="color: #c0c4cc">暂无</span>
          </el-descriptions-item>
          <el-descriptions-item label="等级" :span="2">
            <el-tag v-if="scoreData.gradeLevel" :type="gradeType(scoreData.gradeLevel)" size="large">
              {{ gradeText(scoreData.gradeLevel) }}
            </el-tag>
            <span v-else style="color: #c0c4cc">暂无</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="暂无成绩数据，请等待教师评分" :image-size="80" />
    </el-card>

    <el-empty v-else description="暂无分配的实习计划" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { formatDate } from '@/utils/dateFormat'

const plan = ref(null)
const scoreData = ref(null)
const loading = ref(false)
const userStore = useUserStore()

function gradeType(g) { return { EXCELLENT: 'success', GOOD: '', MEDIUM: 'info', PASS: 'warning', FAIL: 'danger' }[g] || '' }
function gradeText(g) { return { EXCELLENT: '优秀', GOOD: '良好', MEDIUM: '中等', PASS: '及格', FAIL: '不及格' }[g] || g }

const weightItems = computed(() => {
  if (!plan.value) return []
  return [
    { label: '签到', value: plan.value.weightCheckin || 0 },
    { label: '日报', value: plan.value.weightDailyReport || 0 },
    { label: '周报', value: plan.value.weightWeeklyReport || 0 },
    { label: '月报', value: plan.value.weightMonthlyReport || 0 },
    { label: '校内考核', value: plan.value.weightInnerAssessment || 0 },
    { label: '校外考核', value: plan.value.weightOuterAssessment || 0 },
    { label: '实习报告', value: plan.value.weightReport || 0 }
  ]
})

async function loadPlan() {
  loading.value = true
  try {
    const res = await request.get('/internship/plans/my')
    plan.value = res.data || null
    if (plan.value && userStore.isStudent()) {
      await loadScore()
    }
  } catch (e) {
    console.warn('获取实习计划失败', e.message)
  } finally {
    loading.value = false
  }
}

async function loadScore() {
  try {
    const res = await request.get('/internship/scores/my')
    scoreData.value = res.data || null
  } catch (e) {
    console.warn('获取实习成绩失败', e.message)
  }
}

onMounted(() => {
  loadPlan()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.weight-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.weight-tag {
  min-width: 140px;
  text-align: center;
}
</style>
