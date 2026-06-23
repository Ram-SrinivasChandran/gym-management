import { fireEvent, render, screen, waitFor } from '@testing-library/react-native';
import { PaperProvider } from 'react-native-paper';
import LoginScreen from '../src/screens/auth/LoginScreen';
import { useLogin } from '../src/features/auth/useAuth';

jest.mock('../src/features/auth/useAuth', () => ({
  useLogin: jest.fn(),
}));

function renderLoginScreen() {
  return render(
    <PaperProvider>
      <LoginScreen />
    </PaperProvider>
  );
}

describe('LoginScreen', () => {
  let mutate;

  beforeEach(() => {
    mutate = jest.fn();
    useLogin.mockReturnValue({ mutate, isPending: false });
  });

  it('renders email and password fields with a submit button', () => {
    renderLoginScreen();

    expect(screen.getByTestId('login-email-input')).toBeTruthy();
    expect(screen.getByTestId('login-password-input')).toBeTruthy();
    expect(screen.getByTestId('login-submit-button')).toBeTruthy();
  });

  it('shows validation errors and does not submit when fields are invalid', async () => {
    renderLoginScreen();

    fireEvent.press(screen.getByTestId('login-submit-button'));

    await waitFor(() => {
      expect(screen.getByText('Enter a valid email address')).toBeTruthy();
    });
    expect(mutate).not.toHaveBeenCalled();
  });

  it('submits valid credentials to the login mutation', async () => {
    renderLoginScreen();

    fireEvent.changeText(screen.getByTestId('login-email-input'), 'admin@gym.com');
    fireEvent.changeText(screen.getByTestId('login-password-input'), 'password123');
    fireEvent.press(screen.getByTestId('login-submit-button'));

    await waitFor(() => {
      expect(mutate).toHaveBeenCalledWith(
        { email: 'admin@gym.com', password: 'password123' },
        expect.objectContaining({ onError: expect.any(Function) })
      );
    });
  });

  it('shows a server error message when the mutation fails', async () => {
    mutate.mockImplementation((_values, { onError }) => onError());
    renderLoginScreen();

    fireEvent.changeText(screen.getByTestId('login-email-input'), 'admin@gym.com');
    fireEvent.changeText(screen.getByTestId('login-password-input'), 'wrongpassword');
    fireEvent.press(screen.getByTestId('login-submit-button'));

    await waitFor(() => {
      expect(screen.getByText('Invalid email or password')).toBeTruthy();
    });
  });
});
