import { StyleSheet } from 'react-native';
import { TextInput } from 'react-native-paper';

/**
 * Shared form text field. Inputs render on a white field in both light and dark mode, so the
 * typed text must stay dark — otherwise the dark theme's light `onSurface` color renders the
 * text white-on-white and makes it invisible while typing. Centralised so every form gets the
 * fix for free. Any TextInput prop (label, secureTextEntry, keyboardType, error, ...) passes
 * straight through.
 */
export default function AppTextInput({ style, textColor = '#0A0A0A', ...props }) {
  return <TextInput textColor={textColor} style={[styles.field, style]} {...props} />;
}

const styles = StyleSheet.create({
  field: { backgroundColor: '#FFFFFF' },
});
