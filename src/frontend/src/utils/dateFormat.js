export function formatDate(dateStr) {
  if (!dateStr) return ''
  const str = String(dateStr)
  const parts = str.match(/(\d{4})-(\d{1,2})-(\d{1,2})/)
  if (parts) {
    return `${parts[1]}年${parts[2].padStart(2, '0')}月${parts[3].padStart(2, '0')}日`
  }
  return dateStr
}

export function formatDateTime(dtStr) {
  if (!dtStr) return ''
  const str = String(dtStr).replace('T', ' ')
  const parts = str.match(/(\d{4})-(\d{1,2})-(\d{1,2})[T ](\d{1,2}):(\d{2}):(\d{2})/)
  if (parts) {
    return `${parts[1]}年${parts[2].padStart(2, '0')}月${parts[3].padStart(2, '0')}日 ${parts[4].padStart(2, '0')}:${parts[5]}:${parts[6]}`
  }
  return formatDate(dtStr)
}
