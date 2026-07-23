import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Button, Text } from 'react-native-paper';
import { z } from 'zod';
import AppTextInput from '../../components/AppTextInput';
import GradientHeader from '../../components/GradientHeader';
import MemberAvatarPicker from '../../components/MemberAvatarPicker';
import { uploadMemberPhoto } from '../../api/storage';
import { useBranches } from '../../features/branches/useBranches';
import { useCreateMember } from '../../features/members/useMembers';
import { useToastStore } from '../../store/toastStore';

const memberSchema = z.object({
  admissionNumber: z.string().min(1, 'Admission number is required'),
  fullName: z.string().min(1, 'Full name is required'),
  phone: z.string().min(1, 'Phone is required'),
  email: z.union([z.string().email('Enter a valid email'), z.literal('')]).optional(),
  heightCm: z.string().optional(),
  weightKg: z.string().optional(),
  fitnessGoal: z.string().optional(),
});

export default function MemberFormScreen({ navigation }) {
  const { data: branches } = useBranches();
  const createMember = useCreateMember();
  const showToast = useToastStore((state) => state.showToast);
  const [photoAsset, setPhotoAsset] = useState(null);
  const [savingPhoto, setSavingPhoto] = useState(false);

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(memberSchema),
    defaultValues: { admissionNumber: '', fullName: '', phone: '', email: '', heightCm: '', weightKg: '', fitnessGoal: '' },
  });

  const onSubmit = async (values) => {
    // Single-branch gym: members are assigned to the only branch automatically.
    const branchId = branches?.[0]?.id;
    if (!branchId) {
      return;
    }
    try {
      const created = await createMember.mutateAsync({
        branchId,
        admissionNumber: values.admissionNumber,
        fullName: values.fullName,
        phone: values.phone,
        email: values.email || undefined,
        heightCm: values.heightCm ? Number(values.heightCm) : undefined,
        weightKg: values.weightKg ? Number(values.weightKg) : undefined,
        fitnessGoal: values.fitnessGoal || undefined,
      });

      // Photo upload happens after the member exists so the storage path is keyed by the
      // real member id. A failure here shouldn't block the member record from being saved —
      // the admin can add the photo later from the member's detail screen.
      if (photoAsset) {
        setSavingPhoto(true);
        try {
          await uploadMemberPhoto(created.id, photoAsset);
        } catch {
          showToast('Member saved, but the photo upload failed. Add it again from the member profile.');
        } finally {
          setSavingPhoto(false);
        }
      }
      navigation.goBack();
    } catch {
      showToast('Failed to save member. Please try again.');
    }
  };

  return (
    <ScrollView style={styles.flex}>
      <GradientHeader title="New Member" subtitle="Add a member profile" />
      <View style={styles.form}>
        <MemberAvatarPicker
          uri={photoAsset?.uri}
          onPick={setPhotoAsset}
          testID="member-photo-picker"
        />

        <Controller
          control={control}
          name="admissionNumber"
          render={({ field }) => (
            <AppTextInput
              label="Admission Number"
              value={field.value}
              onChangeText={field.onChange}
              style={styles.input}
              testID="member-admission-input"
            />
          )}
        />
        {errors.admissionNumber ? <Text style={styles.errorText}>{errors.admissionNumber.message}</Text> : null}

        <Controller
          control={control}
          name="fullName"
          render={({ field }) => (
            <AppTextInput
              label="Full Name"
              value={field.value}
              onChangeText={field.onChange}
              style={styles.input}
              testID="member-fullname-input"
            />
          )}
        />
        {errors.fullName ? <Text style={styles.errorText}>{errors.fullName.message}</Text> : null}

        <Controller
          control={control}
          name="phone"
          render={({ field }) => (
            <AppTextInput
              label="Phone"
              keyboardType="phone-pad"
              value={field.value}
              onChangeText={field.onChange}
              style={styles.input}
              testID="member-phone-input"
            />
          )}
        />
        {errors.phone ? <Text style={styles.errorText}>{errors.phone.message}</Text> : null}

        <Controller
          control={control}
          name="email"
          render={({ field }) => (
            <AppTextInput
              label="Email (optional)"
              autoCapitalize="none"
              keyboardType="email-address"
              value={field.value}
              onChangeText={field.onChange}
              style={styles.input}
              testID="member-email-input"
            />
          )}
        />
        {errors.email ? <Text style={styles.errorText}>{errors.email.message}</Text> : null}

        <View style={styles.row}>
          <Controller
            control={control}
            name="heightCm"
            render={({ field }) => (
              <AppTextInput
                label="Height (cm)"
                keyboardType="numeric"
                value={field.value}
                onChangeText={field.onChange}
                style={[styles.input, styles.halfInput]}
                testID="member-height-input"
              />
            )}
          />
          <Controller
            control={control}
            name="weightKg"
            render={({ field }) => (
              <AppTextInput
                label="Weight (kg)"
                keyboardType="numeric"
                value={field.value}
                onChangeText={field.onChange}
                style={[styles.input, styles.halfInput]}
                testID="member-weight-input"
              />
            )}
          />
        </View>

        <Controller
          control={control}
          name="fitnessGoal"
          render={({ field }) => (
            <AppTextInput
              label="Fitness Goal (optional)"
              value={field.value}
              onChangeText={field.onChange}
              style={styles.input}
            />
          )}
        />

        <Button
          mode="contained"
          onPress={handleSubmit(onSubmit)}
          loading={createMember.isPending || savingPhoto}
          disabled={createMember.isPending || savingPhoto}
          style={styles.submitButton}
          testID="save-member-button"
        >
          Save Member
        </Button>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  form: { padding: 20 },
  input: { marginTop: 12, backgroundColor: '#FFFFFF' },
  row: { flexDirection: 'row', justifyContent: 'space-between' },
  halfInput: { flexBasis: '48%' },
  errorText: { color: '#EF4444', marginTop: 4, fontSize: 12 },
  submitButton: { marginTop: 24, borderRadius: 10 },
});
