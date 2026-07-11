/** 平台交易货币名称 */
export const CURRENCY_NAME = '秩点'

export function formatCurrency(value?: number | string | null) {
  return Number(value || 0).toFixed(2)
}

export function formatCurrencyWithUnit(value?: number | string | null) {
  return `${formatCurrency(value)} ${CURRENCY_NAME}`
}
