import { StyleSheet, Text, View } from 'react-native';
import { statusColors } from '../theme/colors';

export default function StatusBadge({ status }) {
  const color = statusColors[status] ?? '#94A3B8';

  return (
    <View style={[styles.badge, { backgroundColor: `${color}1F`, borderColor: color }]}>
      <Text style={[styles.text, { color }]}>{status}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
    borderWidth: 1,
    alignSelf: 'flex-start',
  },
  text: {
    fontSize: 12,
    fontWeight: '600',
  },
});
