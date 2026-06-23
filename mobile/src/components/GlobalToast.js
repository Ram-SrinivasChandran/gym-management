import { Snackbar } from 'react-native-paper';
import { useToastStore } from '../store/toastStore';

/**
 * Alert.alert() is a no-op on react-native-web, so all cross-platform user feedback (success
 * and error messages alike) goes through this single Snackbar instead.
 */
export default function GlobalToast() {
  const message = useToastStore((state) => state.message);
  const hideToast = useToastStore((state) => state.hideToast);

  return (
    <Snackbar visible={Boolean(message)} onDismiss={hideToast} duration={3000} testID="global-toast">
      {message}
    </Snackbar>
  );
}
