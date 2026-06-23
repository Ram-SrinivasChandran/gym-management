import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Button, Text, TextInput } from 'react-native-paper';
import { z } from 'zod';
import GradientHeader from '../../components/GradientHeader';
import InlineSelect from '../../components/InlineSelect';
import { useBranches } from '../../features/branches/useBranches';
import { useCreateMember } from '../../features/members/useMembers';

const memberSchema = z.object({
  branchId: z.string().min(1, 'Select a branch'),
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
  const [branchPickerOpen, setBranchPickerOpen] = useState(false);

  const {
    control,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(memberSchema),
    defaultValues: { branchId: '', fullName: '', phone: '', email: '', heightCm: '', weightKg: '', fitnessGoal: '' },
  });

  const selectedBranchId = watch('branchId');
  const selectedBranch = branches?.find((b) => b.id === selectedBranchId);

  const onSubmit = (values) => {
    createMember.mutate(
      {
        branchId: values.branchId,
        fullName: values.fullName,
        phone: values.phone,
        email: values.email || undefined,
        heightCm: values.heightCm ? Number(values.heightCm) : undefined,
        weightKg: values.weightKg ? Number(values.weightKg) : undefined,
        fitnessGoal: values.fitnessGoal || undefined,
      },
      { onSuccess: () => navigation.goBack() }
    );
  };

  return (
    <ScrollView style={styles.flex}>
      <GradientHeader title="New Member" subtitle="Add a member profile" />
      <View style={styles.form}>
        <InlineSelect
          testID="branch-picker"
          placeholder="Select branch"
          selectedLabel={selectedBranch?.name}
          open={branchPickerOpen}
          onToggle={() => setBranchPickerOpen((prev) => !prev)}
          options={(branches ?? []).map((branch) => ({ id: branch.id, label: branch.name }))}
          onSelect={(option) => {
            setValue('branchId', option.id);
            setBranchPickerOpen(false);
          }}
        />
        {errors.branchId ? <Text style={styles.errorText}>{errors.branchId.message}</Text> : null}

        <Controller
          control={control}
          name="fullName"
          render={({ field }) => (
            <TextInput
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
            <TextInput
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
            <TextInput
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
              <TextInput
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
              <TextInput
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
            <TextInput
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
          loading={createMember.isPending}
          disabled={createMember.isPending}
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
