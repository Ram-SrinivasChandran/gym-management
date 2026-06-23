import { StyleSheet, View } from 'react-native';
import { Button } from 'react-native-paper';

/**
 * A dropdown rendered in normal layout flow instead of a Portal/Modal. React Native Paper's
 * <Menu> renders through a Portal that does not reliably appear on the web target (react-native-web),
 * so this is used anywhere a picker needs to work identically on native and web.
 */
export default function InlineSelect({ placeholder, selectedLabel, options, onSelect, open, onToggle, testID }) {
  return (
    <View>
      <Button mode="outlined" onPress={onToggle} style={styles.trigger} testID={testID}>
        {selectedLabel ?? placeholder}
      </Button>
      {open ? (
        <View style={styles.optionList}>
          {options.map((option) => (
            <Button
              key={option.id}
              mode="text"
              onPress={() => onSelect(option)}
              style={styles.option}
              contentStyle={styles.optionContent}
              testID={`${testID}-option-${option.id}`}
            >
              {option.label}
            </Button>
          ))}
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  trigger: { marginTop: 12, backgroundColor: '#FFFFFF' },
  optionList: {
    marginTop: 4,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    backgroundColor: '#FFFFFF',
    overflow: 'hidden',
  },
  option: { borderRadius: 0 },
  optionContent: { justifyContent: 'flex-start' },
});
