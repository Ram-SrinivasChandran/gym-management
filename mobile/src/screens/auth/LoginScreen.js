import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { Image, KeyboardAvoidingView, Platform, StyleSheet, View } from 'react-native';
import { Button, Text, TextInput } from 'react-native-paper';
import AppTextInput from '../../components/AppTextInput';
import { z } from 'zod';
import { sendPasswordReset } from '../../features/auth/api';
import { useLogin } from '../../features/auth/useAuth';

const logoMark = require('../../../assets/logo-mark.png');

const loginSchema = z.object({
  email: z.string().email('Enter a valid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

export default function LoginScreen() {
  const [serverError, setServerError] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const [forgotMsg, setForgotMsg] = useState(null);
  const [sendingReset, setSendingReset] = useState(false);
  const login = useLogin();

  const {
    control,
    handleSubmit,
    getValues,
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

  const onForgotPassword = async () => {
    const email = getValues('email');
    if (!email || !z.string().email().safeParse(email).success) {
      setForgotMsg('Enter your email above first, then tap Forgot Password.');
      return;
    }
    setSendingReset(true);
    setForgotMsg(null);
    try {
      await sendPasswordReset(email);
      setForgotMsg(`Password reset email sent to ${email}. Check your inbox.`);
    } catch {
      setForgotMsg('Could not send the reset email. Please try again.');
    } finally {
      setSendingReset(false);
    }
  };

  return (
    <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <View style={styles.form}>
        <Image source={logoMark} style={styles.logo} resizeMode="contain" />
        <Controller
          control={control}
          name="email"
          render={({ field }) => (
            <AppTextInput
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
            <AppTextInput
              label="Password"
              secureTextEntry={!showPassword}
              value={field.value}
              onChangeText={field.onChange}
              error={Boolean(errors.password)}
              style={styles.input}
              testID="login-password-input"
              right={
                <TextInput.Icon
                  icon={showPassword ? 'eye-off' : 'eye'}
                  onPress={() => setShowPassword((prev) => !prev)}
                  forceTextInputFocus={false}
                  testID="login-password-toggle"
                />
              }
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

        <Button
          mode="text"
          onPress={onForgotPassword}
          loading={sendingReset}
          disabled={sendingReset}
          textColor="#DC2626"
          style={styles.forgotButton}
          testID="forgot-password-button"
        >
          Forgot Password?
        </Button>
        {forgotMsg ? (
          <Text style={styles.forgotHelp} testID="forgot-password-help">
            {forgotMsg}
          </Text>
        ) : null}
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
  forgotButton: { marginTop: 8 },
  forgotHelp: { color: '#475569', textAlign: 'center', marginTop: 4, fontSize: 13 },
});
