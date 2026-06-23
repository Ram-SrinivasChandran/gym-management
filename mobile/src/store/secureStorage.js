import AsyncStorage from '@react-native-async-storage/async-storage';
import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

/**
 * expo-secure-store has no web implementation and hangs/rejects there, so on web we fall back
 * to AsyncStorage (browser localStorage). Native platforms keep using the OS keychain/keystore.
 */
const backend = Platform.OS === 'web' ? AsyncStorage : SecureStore;

export async function getItemAsync(key) {
  return backend.getItem ? backend.getItem(key) : backend.getItemAsync(key);
}

export async function setItemAsync(key, value) {
  return backend.setItem ? backend.setItem(key, value) : backend.setItemAsync(key, value);
}

export async function deleteItemAsync(key) {
  return backend.removeItem ? backend.removeItem(key) : backend.deleteItemAsync(key);
}
