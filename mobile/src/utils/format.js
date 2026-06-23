export function formatCurrency(amount) {
  const value = Number(amount ?? 0);
  return `₹${value.toFixed(2)}`;
}

export function formatDate(isoDateString) {
  if (!isoDateString) return '-';
  const date = new Date(isoDateString);
  if (Number.isNaN(date.getTime())) return '-';
  return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
}

export function computeBmi(heightCm, weightKg) {
  const height = Number(heightCm);
  const weight = Number(weightKg);
  if (!height || !weight || height <= 0) return null;
  const heightM = height / 100;
  return Math.round((weight / (heightM * heightM)) * 100) / 100;
}
