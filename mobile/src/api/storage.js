import { apiClient } from './client';

/**
 * Uploads a picked image (a local file:// URI from expo-image-picker) as the member's profile
 * photo. This goes through our own backend rather than Supabase Storage directly — the
 * client-side Storage upload (user JWT + RLS policies) proved unreliable for this project, so
 * the backend proxies the write using its service_role key instead. Returns the updated member.
 */
export async function uploadMemberPhoto(memberId, asset) {
  const extension = (asset.mimeType?.split('/')?.[1] || 'jpeg').toLowerCase();

  const formData = new FormData();
  formData.append('file', {
    uri: asset.uri,
    type: asset.mimeType || 'image/jpeg',
    name: `photo.${extension}`,
  });

  const response = await apiClient.post(`/members/${memberId}/photo`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
}
