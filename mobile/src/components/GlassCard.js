import { BlurView } from 'expo-blur';
import { Platform, Pressable, StyleSheet, View } from 'react-native';

export default function GlassCard({ children, style, onPress, testID }) {
  const content =
    Platform.OS === 'web' ? (
      <View style={[styles.webFallback, style]}>{children}</View>
    ) : (
      <BlurView intensity={40} tint="light" style={[styles.card, style]}>
        {children}
      </BlurView>
    );

  if (!onPress) {
    return content;
  }

  return (
    <Pressable onPress={onPress} testID={testID}>
      {content}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    overflow: 'hidden',
    padding: 16,
    backgroundColor: 'rgba(255,255,255,0.55)',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.35)',
  },
  webFallback: {
    borderRadius: 16,
    padding: 16,
    backgroundColor: 'rgba(255,255,255,0.85)',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.5)',
  },
});
