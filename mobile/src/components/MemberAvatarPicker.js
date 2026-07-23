import * as ImagePicker from 'expo-image-picker';
import { Image, StyleSheet, View } from 'react-native';
import { Avatar, IconButton, Text } from 'react-native-paper';
import { brand, text } from '../theme/colors';

/**
 * Tappable circular avatar for picking a member's profile photo. Shows the picked/existing
 * photo, or a placeholder with a camera badge. `uri` should be a local file:// URI (freshly
 * picked) or a remote https:// URL (already-saved profilePhotoUrl) — <Image> handles both.
 */
export default function MemberAvatarPicker({ uri, onPick, size = 96, testID }) {
  const requestAndPick = async () => {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.6,
    });
    if (!result.canceled && result.assets?.[0]) {
      onPick(result.assets[0]);
    }
  };

  return (
    <View style={styles.wrap} testID={testID}>
      <View style={{ width: size, height: size }}>
        {uri ? (
          <Image source={{ uri }} style={[styles.image, { width: size, height: size, borderRadius: size / 2 }]} />
        ) : (
          <Avatar.Icon size={size} icon="account" style={styles.placeholder} color="#FFFFFF" />
        )}
        <IconButton
          icon="camera"
          size={18}
          mode="contained"
          containerColor={brand.primary}
          iconColor="#FFFFFF"
          style={styles.badge}
          onPress={requestAndPick}
          testID={`${testID}-camera-button`}
        />
      </View>
      <Text style={styles.hint}>Tap to add photo</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { alignItems: 'center', marginBottom: 8 },
  image: { backgroundColor: '#E2E8F0' },
  placeholder: { backgroundColor: '#94A3B8' },
  badge: { position: 'absolute', right: -4, bottom: -4, margin: 0 },
  hint: { marginTop: 6, fontSize: 12, color: text.muted },
});
