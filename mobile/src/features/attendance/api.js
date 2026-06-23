import { apiClient } from '../../api/client';

export async function checkIn(memberId, method = 'MANUAL') {
  const response = await apiClient.post('/attendance/check-in', { memberId, method });
  return response.data;
}

export async function checkOut(memberId) {
  const response = await apiClient.post(`/attendance/check-out/${memberId}`);
  return response.data;
}

export async function getMemberAttendanceHistory(memberId) {
  const response = await apiClient.get(`/attendance/member/${memberId}/history`);
  return response.data;
}

export async function getAttendanceReport(startDate, endDate) {
  const response = await apiClient.get('/attendance/report', { params: { startDate, endDate } });
  return response.data;
}
