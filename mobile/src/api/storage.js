import { File } from 'expo-file-system';
import { decode } from 'base64-arraybuffer';
import { supabase } from './supabase';

const MEMBER_PHOTOS_BUCKET = 'member-photos';

/**
 * Uploads a picked image (a local file:// URI from expo-image-picker) to the
 * `member-photos` Supabase Storage bucket and returns its public URL. The path is namespaced
 * by memberId with a timestamp suffix so re-uploads don't collide with (or get served stale
 * via CDN cache from) the previous photo.
 */
export async function uploadMemberPhoto(memberId, asset) {
  const extension = (asset.mimeType?.split('/')?.[1] || 'jpg').toLowerCase();
  const path = `${memberId}/${Date.now()}.${extension}`;

  const file = new File(asset.uri);
  const base64 = await file.base64();

  const { error } = await supabase.storage
    .from(MEMBER_PHOTOS_BUCKET)
    .upload(path, decode(base64), {
      contentType: asset.mimeType || 'image/jpeg',
      upsert: true,
    });
  if (error) throw error;

  const { data } = supabase.storage.from(MEMBER_PHOTOS_BUCKET).getPublicUrl(path);
  return data.publicUrl;
}
