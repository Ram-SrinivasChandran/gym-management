import { useState } from 'react';
import { FlatList, StyleSheet, View } from 'react-native';
import { ActivityIndicator, Avatar, FAB, Searchbar, Text } from 'react-native-paper';
import GradientHeader from '../../components/GradientHeader';
import GlassCard from '../../components/GlassCard';
import { useMembersSearch } from '../../features/members/useMembers';
import { text } from '../../theme/colors';

export default function MembersListScreen({ navigation }) {
  const [search, setSearch] = useState('');
  const { data, isLoading, isError } = useMembersSearch({ search });
  const members = data?.content ?? [];

  return (
    <View style={styles.flex}>
      <GradientHeader title="Members" subtitle={`${data?.totalElements ?? 0} total`} />
      <View style={styles.searchWrap}>
        <Searchbar
          placeholder="Search by name, phone, or admission number"
          value={search}
          onChangeText={setSearch}
          testID="members-search-input"
        />
      </View>

      {isLoading ? (
        <ActivityIndicator style={styles.centered} size="large" />
      ) : isError ? (
        <Text style={styles.centered}>Failed to load members.</Text>
      ) : (
        <FlatList
          data={members}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.listContent}
          renderItem={({ item }) => (
            <GlassCard
              style={styles.card}
              onPress={() => navigation.navigate('MemberDetail', { memberId: item.id })}
              testID={`member-card-${item.id}`}
            >
              <View style={styles.cardRow}>
                {item.profilePhotoUrl ? (
                  <Avatar.Image size={40} source={{ uri: item.profilePhotoUrl }} style={styles.avatar} />
                ) : (
                  <Avatar.Icon size={40} icon="account" style={styles.avatar} color="#FFFFFF" />
                )}
                <View style={styles.cardTextWrap}>
                  <Text variant="titleMedium" style={styles.cardTitle}>{item.fullName}</Text>
                  <Text style={styles.meta}>
                    {item.admissionNumber ?? '-'} · {item.phone}
                  </Text>
                </View>
              </View>
            </GlassCard>
          )}
          ListEmptyComponent={<Text style={styles.centered}>No members found.</Text>}
        />
      )}

      <FAB
        icon="plus"
        style={styles.fab}
        onPress={() => navigation.navigate('MemberForm')}
        testID="add-member-fab"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  searchWrap: { paddingHorizontal: 16, marginTop: -20 },
  listContent: { padding: 16, paddingBottom: 96 },
  card: { marginBottom: 12 },
  cardRow: { flexDirection: 'row', alignItems: 'center' },
  avatar: { backgroundColor: '#94A3B8' },
  cardTextWrap: { marginLeft: 12, flexShrink: 1 },
  cardTitle: { color: text.title, fontWeight: '700' },
  meta: { color: text.muted, marginTop: 4 },
  centered: { textAlign: 'center', marginTop: 40 },
  fab: { position: 'absolute', right: 16, bottom: 16, backgroundColor: '#DC2626' },
});
