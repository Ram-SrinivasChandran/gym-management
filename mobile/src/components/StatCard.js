import { useEffect } from 'react';
import { StyleSheet, Text } from 'react-native';
import Animated, { useAnimatedStyle, useSharedValue, withTiming } from 'react-native-reanimated';
import GlassCard from './GlassCard';

export default function StatCard({ label, value, color, style }) {
  const opacity = useSharedValue(0);
  const translateY = useSharedValue(12);

  useEffect(() => {
    opacity.value = withTiming(1, { duration: 350 });
    translateY.value = withTiming(0, { duration: 350 });
  }, [opacity, translateY]);

  const animatedStyle = useAnimatedStyle(() => ({
    opacity: opacity.value,
    transform: [{ translateY: translateY.value }],
  }));

  const slug = label.toLowerCase().replace(/[^a-z0-9]+/g, '-');

  return (
    <Animated.View style={[styles.wrapper, animatedStyle, style]} testID={`stat-card-${slug}`}>
      <GlassCard>
        <Text style={styles.label}>{label}</Text>
        <Text style={[styles.value, color ? { color } : null]} testID={`stat-value-${slug}`}>
          {value}
        </Text>
      </GlassCard>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    flexBasis: '48%',
    marginBottom: 12,
  },
  label: {
    fontSize: 13,
    color: '#64748B',
    marginBottom: 6,
  },
  value: {
    fontSize: 22,
    fontWeight: '700',
    color: '#0F172A',
  },
});
