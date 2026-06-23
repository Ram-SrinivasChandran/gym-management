import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { Image, KeyboardAvoidingView, Platform, StyleSheet, View } from 'react-native';
import { Button, Text, TextInput } from 'react-native-paper';
import { z } from 'zod';
import { useLogin } from '../../features/auth/useAuth';

const logoMark = require('../../../assets/logo-mark.png');

const loginSchema = z.object({
  email: z.string().email('Enter a valid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

export default function LoginScreen() {
  const [serverError, setServerError] = useState(null);
  const login = useLogin();

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });

  const onSubmit = (values) => {
    setServerError(null);
    login.mutate(values, {
      onError: () => setServerError('Invalid email or password'),
    });
  };

  return (
    <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <View style={styles.form}>
        <Image source={logoMark} style={styles.logo} resizeMode="contain" />
        <Controller
          control={control}
          name="email"
          render={({ field }) => (
            <TextInput
              label="Email"
              autoCapitalize="none"
              keyboardType="email-address"
              value={field.value}
              onChangeText={field.onChange}
              error={Boolean(errors.email)}
              style={styles.input}
              testID="login-email-input"
            />
          )}
        />
        {errors.email ? <Text style={styles.errorText}>{errors.email.message}</Text> : null}

        <Controller
          control={control}
          name="password"
          render={({ field }) => (
            <TextInput
              label="Password"
              secureTextEntry
              value={field.value}
              onChangeText={field.onChange}
              error={Boolean(errors.password)}
              style={styles.input}
              testID="login-password-input"
            />
          )}
        />
        {errors.password ? <Text style={styles.errorText}>{errors.password.message}</Text> : null}

        {serverError ? <Text style={styles.errorText}>{serverError}</Text> : null}

        <Button
          mode="contained"
          onPress={handleSubmit(onSubmit)}
          loading={login.isPending}
          disabled={login.isPending}
          style={styles.submitButton}
          buttonColor="#EF4444"
          textColor="#FFFFFF"
          testID="login-submit-button"
        >
          Sign In
        </Button>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  form: { flex: 1, padding: 24, justifyContent: 'center' },
  logo: { width: 220, height: 150, alignSelf: 'center', marginBottom: 32, borderRadius: 16 },
  input: { marginTop: 16, backgroundColor: '#FFFFFF' },
  errorText: { color: '#EF4444', marginTop: 4, fontSize: 12 },
  submitButton: { marginTop: 24, borderRadius: 10 },
});
