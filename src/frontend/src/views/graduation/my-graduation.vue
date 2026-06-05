<template>
  <div class="my-graduation-container">
    <el-collapse v-model="activeNames" class="custom-collapse">
      <!-- 1. 毕业设计批次信息 -->
      <el-collapse-item name="batch">
        <template #title>
          <div class="collapse-title">
            <span class="title-icon">📋</span>
            <span class="title-text">毕业设计批次信息</span>
            <el-tag :type="batchInfo.status === '进行中' ? 'success' : batchInfo.status === '未开始' ? 'info' : 'info'" size="small" class="status-tag">
              {{ batchInfo.status }}
            </el-tag>
          </div>
        </template>

        <div class="collapse-content">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="批次名称">{{ batchInfo.batchName }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ batchInfo.semester }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ batchInfo.startDate }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ batchInfo.endDate }}</el-descriptions-item>
            <el-descriptions-item label="当前阶段">{{ batchInfo.currentPhase }}</el-descriptions-item>
            <el-descriptions-item label="指导老师">{{ batchInfo.teacherName }}</el-descriptions-item>
            <el-descriptions-item label="所属专业/班级" :span="2">{{ batchInfo.major }} / {{ batchInfo.className }}</el-descriptions-item>
          </el-descriptions>

          <div class="timeline-section">
            <h4>📅 重要时间节点</h4>
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in batchInfo.timeline"
                :key="index"
                :timestamp="item.date"
                :type="item.type"
                placement="top"
              >
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </el-collapse-item>

      <!-- 2. 毕业设计题目信息 -->
      <el-collapse-item name="topic">
        <template #title>
          <div class="collapse-title">
            <span class="title-icon">📝</span>
            <span class="title-text">毕业设计题目信息</span>
            <el-tag :type="getTopicStatusType(topicInfo.status)" size="small" class="status-tag">
              {{ topicInfo.status }}
            </el-tag>
          </div>
        </template>

        <div class="collapse-content">
          <el-alert
            v-if="topicInfo.status === '待选择'"
            title="您尚未选择毕业设计题目"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 16px;"
          >
            <template #default>
              请尽快在规定时间内选择您的毕设题目。您可以浏览题库并提交选题申请。
            </template>
          </el-alert>

          <el-descriptions :column="1" border v-if="topicInfo.topicName">
            <el-descriptions-item label="题目名称">
              <strong style="font-size: 16px; color: #303133;">{{ topicInfo.topicName }}</strong>
            </el-descriptions-item>
            <el-descriptions-item label="题目类型">{{ topicInfo.topicType }}</el-descriptions-item>
            <el-descriptions-item label="题目来源">{{ topicInfo.source }}</el-descriptions-item>
            <el-descriptions-item label="难度等级">
              <el-rate v-model="topicInfo.difficulty" disabled />
              <span style="margin-left: 8px;">{{ ['简单', '较易', '中等', '较难', '困难'][topicInfo.difficulty - 1] }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="选择时间">{{ topicInfo.selectTime }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="topicInfo.approved ? 'success' : 'warning'" size="small">
                {{ topicInfo.approved ? '✅ 已通过' : '⏳ 待审核' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="题目描述" :span="2">
              <div class="description-content">{{ topicInfo.description }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="技术要求" :span="2">
              <div class="description-content">{{ topicInfo.requirements }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="参考文献" :span="2">
              <div class="reference-list">
                <p v-for="(ref, index) in topicInfo.references" :key="index">· {{ ref }}</p>
              </div>
            </el-descriptions-item>
          </el-descriptions>

          <div class="action-buttons" v-if="topicInfo.status === '待选择'">
            <el-button type="primary" @click="handleSelectTopic">
              <el-icon><Search /></el-icon>
              浏览题库并选择
            </el-button>
          </div>
        </div>
      </el-collapse-item>

      <!-- 3. 毕业设计任务书信息 -->
      <el-collapse-item name="taskbook">
        <template #title>
          <div class="collapse-title">
            <span class="title-icon">📄</span>
            <span class="title-text">毕业设计任务书信息</span>
            <el-tag :type="getTaskBookStatusType(taskBookInfo.status)" size="small" class="status-tag">
              {{ taskBookInfo.status }}
            </el-tag>
          </div>
        </template>

        <div class="collapse-content">
          <el-alert
            v-if="taskBookInfo.status === '未下达'"
            title="任务书尚未下达"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px;"
          >
            <template #default>
              您的指导老师还未下达任务书，请耐心等待或联系老师。
            </template>
          </el-alert>

          <el-descriptions :column="2" border v-if="taskBookInfo.content">
            <el-descriptions-item label="下达时间">{{ taskBookInfo.issueTime }}</el-descriptions-item>
            <el-descriptions-item label="完成期限">{{ taskBookInfo.deadline }}</el-descriptions-item>
            <el-descriptions-item label="任务书状态">
              <el-tag :type="taskBookInfo.confirmed ? 'success' : 'warning'" size="small">
                {{ taskBookInfo.confirmed ? '✅ 已确认' : '⏳ 待确认' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="指导老师">{{ taskBookInfo.teacherName }}</el-descriptions-item>
            <el-descriptions-item label="主要任务" :span="2">
              <div class="task-content">{{ taskBookInfo.mainTask }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="基本要求" :span="2">
              <div class="requirement-list">
                <p v-for="(req, index) in taskBookInfo.requirements" :key="index">
                  <el-icon><Check /></el-icon> {{ req }}
                </p>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="技术参数" :span="2">
              <div class="tech-params">
                <el-table :data="taskBookInfo.techParams" size="small" border>
                  <el-table-column prop="name" label="参数名称" width="200" />
                  <el-table-column prop="value" label="要求值" />
                  <el-table-column prop="note" label="备注" />
                </el-table>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="参考资料" :span="2">
              <div class="reference-list">
                <p v-for="(ref, index) in taskBookInfo.references" :key="index">· {{ ref }}</p>
              </div>
            </el-descriptions-item>
          </el-descriptions>

          <div class="action-buttons" v-if="taskBookInfo.status === '已下达' && !taskBookInfo.confirmed">
            <el-button type="success" @click="handleConfirmTaskBook">
              <el-icon><Check /></el-icon>
              确认接收任务书
            </el-button>
          </div>

          <div class="action-buttons" v-if="taskBookInfo.content">
            <el-button type="primary" plain @click="handleDownloadTaskBook">
              <el-icon><Download /></el-icon>
              下载任务书PDF
            </el-button>
          </div>
        </div>
      </el-collapse-item>

      <!-- 4. 毕业设计中期检查信息 -->
      <el-collapse-item name="midterm">
        <template #title>
          <div class="collapse-title">
            <span class="title-icon">✏️</span>
            <span class="title-text">毕业设计中期检查信息</span>
            <el-tag :type="getMidtermStatusType(midtermInfo.status)" size="small" class="status-tag">
              {{ midtermInfo.status }}
            </el-tag>
          </div>
        </template>

        <div class="collapse-content">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getMidtermStatusType(midtermInfo.status)" size="large">
                {{ midtermInfo.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最新进度">
              <el-progress
                :percentage="midtermInfo.progress"
                :status="getProgressStatus(midtermInfo.progress)"
                :stroke-width="20"
                :text-inside="true"
                style="width: 200px;"
              />
            </el-descriptions-item>
            <el-descriptions-item label="最近提交时间">{{ midtermInfo.lastSubmitTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="提交次数">{{ midtermInfo.submitCount }} 次</el-descriptions-item>
            <el-descriptions-item label="老师评语" :span="2" v-if="midtermInfo.teacherComment">
              <div class="teacher-comment-box">
                <div class="comment-label">💬 指导老师意见</div>
                <div class="comment-content">{{ midtermInfo.teacherComment }}</div>
              </div>
            </el-descriptions-item>
          </el-descriptions>

          <div class="report-section" v-if="midtermInfo.reports && midtermInfo.reports.length > 0">
            <h4>📊 历次中期检查报告</h4>
            <el-timeline>
              <el-timeline-item
                v-for="(report, index) in midtermInfo.reports"
                :key="index"
                :timestamp="report.submitTime"
                :type="report.status === '已通过' ? 'success' : report.status === '需修改' ? 'danger' : 'warning'"
                placement="top"
              >
                <el-card shadow="hover" class="report-card">
                  <div class="report-header">
                    <strong>第 {{ report.version }} 次提交</strong>
                    <el-tag :type="report.status === '已通过' ? 'success' : report.status === '需修改' ? 'danger' : 'warning'" size="small">
                      {{ report.status }}
                    </el-tag>
                  </div>
                  <div class="report-progress">
                    <span>完成进度：</span>
                        <el-progress
                          :percentage="report.progress"
                          :status="getProgressStatus(report.progress)"
                          :stroke-width="10"
                          style="width: 150px; display: inline-block;"
                        />
                      </div>
                      <div class="report-content">
                        <p><strong>报告摘要：</strong>{{ report.summary }}</p>
                      </div>
                      <div class="report-comment" v-if="report.comment">
                        <p><strong>老师反馈：</strong>{{ report.comment }}</p>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>

              <div class="action-buttons" v-if="['未提交', '需修改'].includes(midtermInfo.status)">
                <el-button type="primary" @click="handleSubmitMidtermReport">
                  <el-icon><Upload /></el-icon>
                  提交中期检查报告
                </el-button>
              </div>
            </div>
          </el-collapse-item>

          <!-- 5. 毕业设计最终检查信息 -->
          <el-collapse-item name="final">
            <template #title>
              <div class="collapse-title">
                <span class="title-icon">🎯</span>
                <span class="title-text">毕业设计最终检查信息</span>
                <el-tag :type="getFinalStatusType(finalInfo.status)" size="small" class="status-tag">
                  {{ finalInfo.status }}
                  <span v-if="finalInfo.status === '已定稿'" style="margin-left: 4px;">✨</span>
                </el-tag>
              </div>
            </template>

            <div class="collapse-content">
              <el-alert
                v-if="finalInfo.status === '已定稿'"
                title="🎉 恭喜！您的毕设报告已定稿"
                type="success"
                :closable="false"
                show-icon
                style="margin-bottom: 16px;"
              >
                <template #default>
                  您的毕设报告已于 <strong>{{ finalInfo.finalizeTime }}</strong> 正式定稿，该版本将用于学校存档和答辩评审。
                </template>
              </el-alert>

              <el-descriptions :column="2" border>
                <el-descriptions-item label="定稿状态">
                  <el-tag :type="getFinalStatusType(finalInfo.status)" size="large" effect="dark">
                    {{ finalInfo.status }}
                    <el-icon v-if="finalInfo.status === '已定稿'" style="margin-left: 4px;"><Finished /></el-icon>
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="当前版本">
                  <el-tag type="primary" effect="dark">V{{ finalInfo.version }}.0</el-tag>
                  <span v-if="finalInfo.status === '已定稿'" style="margin-left: 8px; color: #67c23a; font-weight: bold;">(最终版)</span>
                </el-descriptions-item>
                <el-descriptions-item label="定稿时间" v-if="finalInfo.finalizeTime">
                  <span style="color: #67c23a; font-weight: bold;">{{ finalInfo.finalizeTime }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="提交次数">{{ finalInfo.submitCount }} 次</el-descriptions-item>
                <el-descriptions-item label="教师评语（存档）" :span="2" v-if="finalInfo.teacherComment">
                  <div class="teacher-comment-archive">
                    <div class="archive-label">⭐ 该评语将作为正式教师评语存档</div>
                    <div class="archive-content">{{ finalInfo.teacherComment }}</div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>

              <div class="report-preview" v-if="finalInfo.reportContent">
                <h4>📄 最终版毕设报告预览</h4>
                <div class="preview-content">
                  <pre>{{ finalInfo.reportContent }}</pre>
                </div>
              </div>

              <div class="revision-history" v-if="finalInfo.history && finalInfo.history.length > 0">
                <h4>📜 版本修改记录</h4>
                <el-timeline>
                  <el-timeline-item
                    v-for="(item, index) in finalInfo.history"
                    :key="index"
                    :timestamp="item.time"
                    :type="item.action === '通过并定稿' ? 'success' : item.action === '驳回' ? 'danger' : 'primary'"
                    placement="top"
                  >
                    <div class="history-item">
                      <strong>V{{ item.version }}.0 - {{ item.action }}</strong>
                      <p v-if="item.comment">{{ item.comment }}</p>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>

              <div class="action-buttons" v-if="['未提交', '需修改'].includes(finalInfo.status)">
                <el-button type="success" @click="handleSubmitFinalReport">
                  <el-icon><Upload /></el-icon>
                  提交最终毕设报告
                </el-button>
              </div>
            </div>
          </el-collapse-item>

          <!-- 6. 毕业设计答辩信息 -->
          <el-collapse-item name="defense">
            <template #title>
              <div class="collapse-title">
                <span class="title-icon">🎤</span>
                <span class="title-text">毕业设计论文评阅信息（答辩）</span>
                <el-tag :type="getDefenseStatusType(defenseInfo.status)" size="small" class="status-tag">
                  {{ defenseInfo.status }}
                </el-tag>
              </div>
            </template>

            <div class="collapse-content">
              <el-alert
                v-if="defenseInfo.status === '未答辩'"
                title="答辩安排"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 16px;"
              >
                <template #default>
                  答辩时间：<strong>{{ defenseInfo.scheduledTime || '待通知' }}</strong><br/>
                  答辩地点：<strong>{{ defenseInfo.location || '待通知' }}</strong><br/>
                  请提前准备好答辩PPT和相关材料。
                </template>
              </el-alert>

              <el-descriptions :column="2" border>
                <el-descriptions-item label="答辩状态">
                  <el-tag :type="getDefenseStatusType(defenseInfo.status)" size="large">
                    {{ defenseInfo.status }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="答辩成绩" v-if="defenseInfo.score !== null">
                  <span style="font-size: 24px; font-weight: bold; color: #409eff;">{{ defenseInfo.score }}</span>
                  <span style="margin-left: 8px;">分</span>
                </el-descriptions-item>
                <el-descriptions-item label="成绩等级" v-if="defenseInfo.grade">
                  <el-tag :type="getGradeType(defenseInfo.grade)" size="large" effect="dark">
                    {{ defenseInfo.grade }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="答辩日期" v-if="defenseInfo.defenseDate">{{ defenseInfo.defenseDate }}</el-descriptions-item>
                <el-descriptions-item label="答辩地点" v-if="defenseInfo.location">{{ defenseInfo.location }}</el-descriptions-item>
                <el-descriptions-item label="答辩委员会" v-if="defenseInfo.committee">{{ defenseInfo.committee }}</el-descriptions-item>
                <el-descriptions-item label="答辩评语" :span="2" v-if="defenseInfo.comment">
                  <div class="defense-comment">
                    <div class="comment-label">📝 答辩委员会评语</div>
                    <div class="comment-text">{{ defenseInfo.comment }}</div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>

              <div class="document-section" v-if="defenseInfo.pdfFileName">
                <h4>📋 答辩相关文档</h4>
                <el-card shadow="hover">
                  <div class="file-item">
                    <el-icon style="color: #f56c6c; font-size: 32px;"><Document /></el-icon>
                    <div class="file-info">
                      <div class="file-name">{{ defenseInfo.pdfFileName }}</div>
                      <div class="file-action">
                        <el-button type="primary" link size="small" @click="handleViewDefensePdf">
                          <el-icon><View /></el-icon>
                          在线查看
                        </el-button>
                        <el-button type="success" link size="small" @click="handleDownloadDefensePdf">
                          <el-icon><Download /></el-icon>
                          下载文档
                        </el-button>
                      </div>
                    </div>
                  </div>
                </el-card>
              </div>

              <div class="tips-section">
                <el-alert
                  title="💡 温馨提示"
                  type="warning"
                  :closable="false"
                  show-icon
                >
                  <template #default>
                    <ul style="margin: 0; padding-left: 20px;">
                      <li>请携带学生证、身份证参加答辩</li>
                      <li>建议提前15分钟到达答辩地点</li>
                      <li>准备5-8分钟的PPT汇报</li>
                      <li>准备好纸质版毕设论文（按要求的份数）</li>
                    </ul>
                  </template>
                </el-alert>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </template>

    <script setup>
    import { ref } from 'vue'
    import { Search, Check, Download, Upload, View, Document, Finished } from '@element-plus/icons-vue'
    import { ElMessage, ElMessageBox } from 'element-plus'

    const activeNames = ref(['batch'])

    const batchInfo = ref({
      status: '进行中',
      batchName: '2025届本科毕业设计',
      semester: '2025-2026学年第1学期',
      startDate: '2026-03-01',
      endDate: '2026-06-30',
      currentPhase: '中期检查阶段',
      teacherName: '廖清科',
      major: '软件工程',
      className: '软工2101班',
      timeline: [
        {
          date: '2026-03-01',
          title: '批次启动',
          description: '毕业设计工作正式启动，开始选题',
          type: 'primary'
        },
        {
          date: '2026-03-15',
          title: '选题截止',
          description: '学生完成毕设题目选择',
          type: ''
        },
        {
          date: '2026-04-01',
          title: '任务书下达',
          description: '指导老师下达任务书',
          type: 'success'
        },
        {
          date: '2026-05-01 - 05-15',
          title: '中期检查',
          description: '提交中期检查报告',
          type: 'warning'
        },
        {
          date: '2026-05-20 - 05-25',
          title: '最终检查',
          description: '提交最终毕设报告',
          type: 'danger'
        },
        {
          date: '2026-06-01 - 06-10',
          title: '答辩环节',
          description: '毕业设计答辩',
          type: 'danger'
        }
      ]
    })

    const topicInfo = ref({
      status: '已选题',
      topicName: '无线宏站勘察系统设计与实现',
      topicType: '工程设计',
      source: '教师命题',
      difficulty: 3,
      selectTime: '2026-03-12 14:30:00',
      approved: true,
      description: '本项目旨在开发一套无线宏站勘察系统，用于辅助通信工程师进行基站选址、勘察数据采集、现场环境评估等工作。系统将集成GIS地图功能，支持现场拍照、数据录入、自动生成勘察报告等核心功能。',
      requirements: '1. 掌握Vue.js前端框架的使用\n2. 熟悉Spring Boot后端开发\n3. 了解GIS地图集成技术\n4. 具备移动端适配能力\n5. 实现数据的导入导出功能',
      references: [
        '[1] 张三. 基于WebGIS的基站选址系统研究[J]. 通信技术, 2025.',
        '[2] 李四. 移动端数据采集技术在工程勘察中的应用[D]. 北京邮电大学, 2024.',
        '[3] 王五. Vue.js企业级应用开发实战[M]. 电子工业出版社, 2025.'
      ]
    })

    const taskBookInfo = ref({
      status: '已下达',
      issueTime: '2026-04-02 09:00:00',
      deadline: '2026-06-15',
      confirmed: true,
      teacherName: '廖清科',
      mainTask: '完成无线宏站勘察系统的设计与实现，包括需求分析、系统设计、编码实现、测试部署等完整软件开发流程。系统需支持PC端和移动端访问，具备良好的用户体验和稳定性。',
      requirements: [
        '完成系统需求分析和技术选型方案',
        '设计完整的数据库结构和API接口',
        '实现用户管理、项目管理、数据采集等核心模块',
        '集成高德/百度地图API，实现地图展示和标注功能',
        '支持现场照片拍摄和数据录入',
        '自动生成标准化的勘察报告（PDF格式）',
        '编写完整的技术文档和用户手册',
        '进行充分的测试和性能优化'
      ],
      techParams: [
        { name: '前端框架', value: 'Vue 3 + Element Plus', note: '响应式设计' },
        { name: '后端框架', value: 'Spring Boot 2.7+', note: 'RESTful API' },
        { name: '数据库', value: 'MySQL 8.0', note: '支持千万级数据' },
        { name: '地图服务', value: '高德地图 API', note: '免费额度内' },
        { name: '文件存储', value: '本地/OSS', note: '图片和PDF' }
      ],
      references: [
        '[1] 尤雨溪. Vue.js官方文档[EB/OL]. https://cn.vuejs.org/',
        '[2] Spring团队. Spring Boot Reference Documentation[EB/OL].',
        '[3] 高德开放平台. 地图JavaScript API文档[EB/OL].'
      ]
    })

    const midtermInfo = ref({
      status: '已通过',
      progress: 75,
      lastSubmitTime: '2026-05-14 16:45:00',
      submitCount: 2,
      teacherComment: '进度良好，系统主体功能已完成80%。建议加快数据库优化和报告生成功能的开发，确保按时完成最终版。代码质量较好，继续保持。',
      reports: [
        {
          version: 1,
          submitTime: '2026-05-02 10:30:00',
          progress: 40,
          status: '需修改',
          summary: '完成了系统架构设计和部分基础功能开发，包括登录注册、项目列表展示等。',
          comment: '1. 需求分析不够详细；2. 缺少数据库ER图；3. 进度稍慢，请加快开发速度。'
        },
        {
          version: 2,
          submitTime: '2026-05-14 16:45:00',
          progress: 75,
          status: '已通过',
          summary: '根据上次反馈进行了改进，新增了数据采集模块、地图集成功能，完善了需求文档。目前整体进度符合预期。',
          comment: '进度良好，系统主体功能已完成80%。建议加快后续开发。'
        }
      ]
    })

    const finalInfo = ref({
      status: '需修改',
      version: 2,
      submitCount: 2,
      finalizeTime: null,
      teacherComment: '',
      reportContent: `一、项目概述

本项目为"无线宏站勘察系统设计与实现"，旨在为通信行业提供一套高效、便捷的基站勘察工具。

二、系统架构

2.1 技术架构
采用前后端分离的B/S架构：
- 前端：Vue 3 + Element Plus + ECharts
- 后端：Spring Boot + MyBatis-Plus
- 数据库：MySQL 8.0 + Redis
- 文件存储：MinIO对象存储

2.2 功能模块
1. 用户权限管理模块
2. 项目与任务管理模块
3. GIS地图展示模块
4. 数据采集与录入模块
5. 报告自动生成模块
6. 系统管理与统计模块

三、核心功能实现

3.1 地图集成
使用高德地图JavaScript API，实现了：
- 基站位置标注与可视化
- 路径规划与距离测量
- 卫星图/地形图切换

3.2 数据采集
支持多种数据类型采集：
- 基础信息（经纬度、海拔、天线高度等）
- 现场照片（支持多角度拍摄）
- 环境参数（信号强度、遮挡情况等）

3.3 报告生成
基于iTextPDF库，自动生成包含：
- 项目基本信息
- 勘察数据汇总
- 现场照片附件
- 专业图表分析
- 结论与建议

四、测试结果

经过充分的功能测试和性能测试：
- 功能测试用例通过率：98%
- 页面平均响应时间：< 1.5秒
- 并发用户支持：100+
- 系统可用性：99.9%

五、总结与展望

本项目成功实现了无线宏站勘察的全流程数字化管理，显著提升了工作效率。未来可进一步扩展AI智能分析、移动端APP等功能。

（报告内容省略...）`,
      history: [
        {
          version: 1,
          action: '首次提交',
          time: '2026-05-21 10:00:00',
          comment: ''
        },
        {
          version: 2,
          action: '驳回',
          time: '2026-05-23 15:30:00',
          comment: '1. 测试部分不够详细，缺少具体的测试用例；2. 性能测试数据需要补充；3. 部分截图模糊不清。请在一周内修改完毕重新提交。'
        }
      ]
    })

    const defenseInfo = ref({
      status: '未答辩',
      score: null,
      grade: null,
      scheduledTime: '2026-06-05 14:00',
      location: '教学楼A301',
      defenseDate: null,
      committee: null,
      comment: null,
      pdfFileName: null
    })

    function getTopicStatusType(status) {
      const map = {
        '待选择': 'info',
        '已选题': 'success',
        '审核中': 'warning'
      }
      return map[status] || 'info'
    }

    function getTaskBookStatusType(status) {
      const map = {
        '未下达': 'info',
        '已下达': 'warning',
        '已确认': 'success'
      }
      return map[status] || 'info'
    }

    function getMidtermStatusType(status) {
      const map = {
        '未提交': 'info',
        '待审核': 'warning',
        '已通过': 'success',
        '需修改': 'danger'
      }
      return map[status] || 'info'
    }

    function getFinalStatusType(status) {
      const map = {
        '未提交': 'info',
        '待审核': 'warning',
        '已定稿': 'success',
        '需修改': 'danger'
      }
      return map[status] || 'info'
    }

    function getDefenseStatusType(status) {
      const map = {
        '未答辩': 'info',
        '待提交': 'warning',
        '待审核': 'warning',
        '已通过': 'success',
        '需修改': 'danger'
      }
      return map[status] || 'info'
    }

    function getGradeType(grade) {
      const map = {
        '优秀': 'success',
        '良好': '',
        '中等': 'warning',
        '及格': 'info',
        '不及格': 'danger'
      }
      return map[grade] || 'info'
    }

    function getProgressStatus(progress) {
      if (progress >= 90) return 'success'
      if (progress >= 70) return ''
      if (progress >= 50) return 'warning'
      return 'exception'
    }

    function handleSelectTopic() {
      ElMessage.info('跳转到题库浏览页面...')
    }

    function handleConfirmTaskBook() {
      ElMessageBox.confirm('确定要确认接收任务书吗？确认后将开始按照任务书要求开展工作。', '确认接收', {
        confirmButtonText: '确定确认',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        taskBookInfo.value.confirmed = true
        ElMessage.success('✅ 已成功确认接收任务书')
      }).catch(() => {})
    }

    function handleDownloadTaskBook() {
      ElMessage.success('正在下载任务书PDF...')
    }

    function handleSubmitMidtermReport() {
      ElMessage.info('打开中期检查报告提交页面...')
    }

    function handleSubmitFinalReport() {
      ElMessage.info('打开最终毕设报告提交页面...')
    }

    function handleViewDefensePdf() {
      ElMessage.info('正在加载PDF预览...')
    }

    function handleDownloadDefensePdf() {
      ElMessage.success('正在下载答辩记录表...')
    }
    </script>

    <style scoped>
    .my-graduation-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.custom-collapse {
      background: transparent;
    }

    .custom-collapse :deep(.el-collapse-item) {
      margin-bottom: 12px;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    }

    .custom-collapse :deep(.el-collapse-item__header) {
      background: #fff;
      height: auto;
      min-height: 60px;
      padding: 16px 20px;
      border: none;
      font-size: 16px;
      font-weight: 500;
    }

    .custom-collapse :deep(.el-collapse-item__wrap) {
      background: #fafbfc;
      border: none;
    }

    .custom-collapse :deep(.el-collapse-item__content) {
      padding: 20px;
    }

    .collapse-title {
      display: flex;
      align-items: center;
      width: 100%;
      gap: 12px;
    }

    .title-icon {
      font-size: 24px;
    }

    .title-text {
      flex: 1;
      text-align: left;
    }

    .status-tag {
      flex-shrink: 0;
    }

    .collapse-content {
      color: #606266;
    }

    .timeline-section {
      margin-top: 20px;
      padding: 16px;
      background: #fff;
      border-radius: 6px;
    }

    .timeline-section h4 {
      margin: 0 0 16px 0;
      color: #303133;
    }

    .description-content,
    .task-content {
      white-space: pre-wrap;
      line-height: 1.8;
      color: #303133;
      font-size: 14px;
    }

    .requirement-list p,
    .reference-list p {
      margin: 6px 0;
      line-height: 1.8;
      color: #606266;
    }

    .requirement-list .el-icon {
      color: #67c23a;
      margin-right: 6px;
    }

    .tech-params {
      margin-top: 8px;
    }

    .action-buttons {
      margin-top: 20px;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;
      display: flex;
      justify-content: center;
      gap: 12px;
    }

    .teacher-comment-box {
      border: 2px solid #e6a23c;
      border-radius: 6px;
      padding: 12px;
      background: #fdf6ec;
    }

    .comment-label {
      color: #e6a23c;
      font-weight: bold;
      margin-bottom: 8px;
      font-size: 13px;
    }

    .comment-content {
      white-space: pre-wrap;
      line-height: 1.8;
      color: #303133;
    }

    .report-section {
      margin-top: 20px;
    }

    .report-section h4 {
      margin: 0 0 16px 0;
      color: #303133;
    }

    .report-card {
      margin-bottom: 12px;
    }

    .report-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .report-progress {
      margin: 8px 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .report-content p,
    .report-comment p {
      margin: 8px 0;
      line-height: 1.6;
      color: #606266;
    }

    .teacher-comment-archive {
      border: 2px solid #67c23a;
      border-radius: 6px;
      padding: 16px;
      background: #f0f9eb;
    }

    .archive-label {
      color: #67c23a;
      font-weight: bold;
      margin-bottom: 8px;
      font-size: 14px;
    }

    .archive-content {
      white-space: pre-wrap;
      line-height: 1.8;
      color: #303133;
      font-size: 14px;
    }

    .report-preview {
      margin-top: 20px;
    }

    .report-preview h4 {
      margin: 0 0 12px 0;
      color: #303133;
    }

    .preview-content {
      max-height: 400px;
      overflow-y: auto;
      background: #fff;
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      padding: 16px;
    }

    .preview-content pre {
      margin: 0;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-family: 'Microsoft YaHei', Arial, sans-serif;
      font-size: 13px;
      line-height: 1.8;
      color: #303133;
    }

    .revision-history {
      margin-top: 20px;
    }

    .revision-history h4 {
      margin: 0 0 16px 0;
      color: #303133;
    }

    .history-item strong {
      color: #303133;
    }

    .history-item p {
      margin: 6px 0 0 0;
      color: #606266;
      font-size: 13px;
    }

    .document-section {
      margin-top: 20px;
    }

    .document-section h4 {
      margin: 0 0 12px 0;
      color: #303133;
    }

    .file-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 12px;
    }

    .file-info {
      flex: 1;
    }

    .file-name {
      font-size: 15px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 8px;
    }

    .file-action {
      display: flex;
      gap: 8px;
    }

    .defense-comment {
      border: 2px solid #409eff;
      border-radius: 6px;
      padding: 16px;
      background: #ecf5ff;
    }

    .defense-comment .comment-label {
      color: #409eff;
    }

    .defense-comment .comment-text {
      white-space: pre-wrap;
      line-height: 1.8;
      color: #303133;
      font-size: 14px;
    }

    .tips-section {
      margin-top: 20px;
    }

    .tips-section ul li {
      line-height: 2;
      color: #606266;
    }
    </style>
