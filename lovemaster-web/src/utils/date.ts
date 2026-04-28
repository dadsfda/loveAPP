export function daysBetween(startDate?: string | null) {
  if (!startDate) {
    return 0;
  }
  const start = new Date(startDate);
  const today = new Date();
  start.setHours(0, 0, 0, 0);
  today.setHours(0, 0, 0, 0);
  return Math.max(0, Math.floor((today.getTime() - start.getTime()) / 86400000) + 1);
}

export function daysUntil(date: string) {
  const target = new Date(date);
  const today = new Date();
  target.setHours(0, 0, 0, 0);
  today.setHours(0, 0, 0, 0);
  const thisYear = new Date(today.getFullYear(), target.getMonth(), target.getDate());
  const next = thisYear < today ? new Date(today.getFullYear() + 1, target.getMonth(), target.getDate()) : thisYear;
  return Math.ceil((next.getTime() - today.getTime()) / 86400000);
}

export function formatDate(date?: string | null) {
  if (!date) {
    return '尚未记录';
  }
  return date.slice(0, 10).replace(/-/g, '.');
}
