import { configureStore } from '@reduxjs/toolkit';
import { render, screen } from '@testing-library/react';
import type { ReactElement } from 'react';
import { Provider } from 'react-redux';

import { Can } from '@/features/authorization/components/can';
import { PermissionProvider } from '@/features/authorization/providers/permission-provider';
import { RoleProvider } from '@/features/authorization/providers/role-provider';
import { authorizationReducer } from '@/features/authorization/store/authorization-slice';

function renderWithAuthz(
  ui: ReactElement,
  options: { roles?: string[]; permissions?: string[] } = {},
) {
  const store = configureStore({
    reducer: { authorization: authorizationReducer },
    preloadedState: {
      authorization: {
        status: 'ready' as const,
        userId: 'u1',
        tenantId: 't1',
        email: 'a@b.com',
        roles: options.roles ?? ['HOSPITAL_ADMIN'],
        permissions: options.permissions ?? ['HOSPITAL_READ'],
        error: null,
      },
    },
  });

  return render(
    <Provider store={store}>
      <RoleProvider>
        <PermissionProvider>{ui}</PermissionProvider>
      </RoleProvider>
    </Provider>,
  );
}

describe('Can', () => {
  it('renders children when permission is granted', () => {
    renderWithAuthz(
      <Can permissions={['HOSPITAL_READ']}>
        <button type="button">Edit hospital</button>
      </Can>,
      { permissions: ['HOSPITAL_READ', 'HOSPITAL_UPDATE'] },
    );

    expect(screen.getByRole('button', { name: 'Edit hospital' })).toBeInTheDocument();
  });

  it('hides unauthorized buttons', () => {
    renderWithAuthz(
      <Can permissions={['USER_DELETE']}>
        <button type="button">Delete user</button>
      </Can>,
      { permissions: ['HOSPITAL_READ'] },
    );

    expect(screen.queryByRole('button', { name: 'Delete user' })).not.toBeInTheDocument();
  });

  it('renders fallback when unauthorized', () => {
    renderWithAuthz(
      <Can permissions={['BILLING_READ']} fallback={<p>No billing access</p>}>
        <p>Billing panel</p>
      </Can>,
      { permissions: [] },
    );

    expect(screen.getByText('No billing access')).toBeInTheDocument();
    expect(screen.queryByText('Billing panel')).not.toBeInTheDocument();
  });
});
