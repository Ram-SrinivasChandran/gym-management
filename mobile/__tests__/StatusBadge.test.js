import { render, screen } from '@testing-library/react-native';
import StatusBadge from '../src/components/StatusBadge';

describe('StatusBadge', () => {
  it('renders the status label for a known status', () => {
    render(<StatusBadge status="ACTIVE" />);
    expect(screen.getByText('ACTIVE')).toBeTruthy();
  });

  it('renders unknown statuses without crashing', () => {
    render(<StatusBadge status="SOMETHING_NEW" />);
    expect(screen.getByText('SOMETHING_NEW')).toBeTruthy();
  });
});
