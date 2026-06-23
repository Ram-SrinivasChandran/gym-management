import { LinearGradient } from 'expo-linear-gradient';
import { Image, StyleSheet, Text, View } from 'react-native';
import { brand } from '../theme/colors';

const logoMark = require('../../assets/logo-mark.png');

export default function GradientHeader({ title, subtitle, right, logoSize = 44 }) {
  return (
    <LinearGradient colors={[brand.primary, brand.dark]} style={styles.container}>
      <View style={styles.row}>
        <Image
          source={logoMark}
          style={[styles.logo, { width: logoSize, height: logoSize }]}
          resizeMode="contain"
        />
        <View style={styles.textBlock}>
          <Text style={styles.title}>{title}</Text>
          {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
        </View>
        {right}
      </View>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 20,
    paddingTop: 24,
    paddingBottom: 32,
    borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
  },
  logo: {
    borderRadius: 10,
    backgroundColor: brand.dark,
    marginRight: 12,
  },
  textBlock: {
    flexShrink: 1,
  },
  title: {
    color: '#FFFFFF',
    fontSize: 24,
    fontWeight: '700',
  },
  subtitle: {
    color: 'rgba(255,255,255,0.85)',
    fontSize: 14,
    marginTop: 4,
  },
});
