'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, MoreHorizontalIcon, PencilIcon } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { FilterSelect } from '@/features/hospital-admin/components/shared/filter-select';
import { ListToolbar } from '@/features/hospital-admin/components/shared/list-toolbar';
import { PaginationControls } from '@/features/hospital-admin/components/shared/pagination-controls';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { useUserMutations, useUsersQuery } from '@/features/hospital-admin/hooks/use-users';
import {
  resetListFilters,
  selectUsersUi,
  setListPage,
  setListSearch,
  setListStatus,
  setUsersEmailVerified,
  setUsersRoleType,
} from '@/features/hospital-admin/store/hospital-admin-ui-slice';
import {
  ROLE_TYPES,
  USER_STATUSES,
  type RoleType,
  type UserManagementStatus,
} from '@/features/hospital-admin/types/enums';
import type {
  UserLifecycleAction,
  UserManagementResponse,
} from '@/features/hospital-admin/types/user';
import {
  adminUpdateUserSchema,
  type AdminUpdateUserFormValues,
} from '@/features/hospital-admin/validation/user-schema';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel, formatPersonName } from '@/lib/page-query';
import { useSession } from '@/providers/session-provider';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

function lifecycleActionsFor(status: UserManagementStatus): Array<{
  action: UserLifecycleAction;
  label: string;
}> {
  switch (status) {
    case 'PENDING':
    case 'INACTIVE':
      return [{ action: 'activate', label: 'Activate' }];
    case 'ACTIVE':
      return [
        { action: 'deactivate', label: 'Deactivate' },
        { action: 'suspend', label: 'Suspend' },
      ];
    case 'SUSPENDED':
      return [{ action: 'restore', label: 'Restore' }];
    case 'LOCKED':
      return [];
    default:
      return [];
  }
}

export function UsersPage() {
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectUsersUi);
  const { user: sessionUser } = useSession();
  const [editing, setEditing] = useState<UserManagementResponse | null>(null);

  const query = useMemo(
    () => ({
      q: ui.q.trim() || undefined,
      status: (ui.status || undefined) as UserManagementStatus | undefined,
      roleType: (ui.roleType || undefined) as RoleType | undefined,
      emailVerified:
        ui.emailVerified === '' ? undefined : ui.emailVerified === 'true' ? true : false,
      page: ui.page,
      size: ui.size,
      sort: ['lastName,asc', 'firstName,asc'],
    }),
    [ui],
  );

  const usersQuery = useUsersQuery(query);
  const mutations = useUserMutations();

  const form = useForm<AdminUpdateUserFormValues>({
    resolver: zodResolver(adminUpdateUserSchema),
    defaultValues: { firstName: '', lastName: '', phone: '' },
  });

  useEffect(() => {
    if (!editing) {
      return;
    }
    form.reset({
      firstName: editing.firstName,
      lastName: editing.lastName,
      phone: editing.phone ?? '',
    });
  }, [editing, form]);

  const onSubmit = form.handleSubmit(async (values) => {
    if (!editing) {
      return;
    }
    try {
      await mutations.update.mutateAsync({
        id: editing.id,
        payload: {
          firstName: values.firstName.trim(),
          lastName: values.lastName.trim(),
          phone: values.phone?.trim() || null,
        },
      });
      toast.success('User profile updated');
      setEditing(null);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to update user'));
    }
  });

  const runLifecycle = async (user: UserManagementResponse, action: UserLifecycleAction) => {
    try {
      await mutations.lifecycle.mutateAsync({ id: user.id, action });
      const labels: Record<UserLifecycleAction, string> = {
        activate: 'User activated',
        deactivate: 'User deactivated',
        suspend: 'User suspended',
        restore: 'User restored',
      };
      toast.success(labels[action]);
    } catch (error) {
      toast.error(getErrorMessage(error, `Unable to ${action} user`));
    }
  };

  const rows = usersQuery.data?.content ?? [];
  const isBusy = mutations.update.isPending || mutations.lifecycle.isPending;

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="User management"
        description="Search hospital users, update profiles, and manage account status without physical deletion."
      />

      <div className="rounded-xl border bg-card">
        <div className="border-b p-4">
          <ListToolbar
            search={ui.q}
            onSearchChange={(q) => dispatch(setListSearch({ key: 'users', q }))}
            searchPlaceholder="Search name, email, phone…"
            showReset={Boolean(ui.q || ui.status || ui.roleType || ui.emailVerified)}
            onReset={() => dispatch(resetListFilters('users'))}
            filters={
              <>
                <FilterSelect
                  value={ui.status}
                  onValueChange={(status) => dispatch(setListStatus({ key: 'users', status }))}
                  options={USER_STATUSES}
                  placeholder="Status"
                  allLabel="All statuses"
                />
                <FilterSelect
                  value={ui.roleType}
                  onValueChange={(roleType) => dispatch(setUsersRoleType(roleType))}
                  options={ROLE_TYPES}
                  placeholder="Role"
                  allLabel="All roles"
                />
                <FilterSelect
                  value={ui.emailVerified}
                  onValueChange={(value) => dispatch(setUsersEmailVerified(value))}
                  options={['true', 'false']}
                  labels={{ true: 'Verified', false: 'Unverified' }}
                  placeholder="Email"
                  allLabel="Any verification"
                />
              </>
            }
          />
        </div>

        {usersQuery.isError ? (
          <EmptyState
            title="Unable to load users"
            description={getErrorMessage(usersQuery.error)}
          />
        ) : usersQuery.isLoading ? (
          <div className="text-muted-foreground px-6 py-16 text-center text-sm">Loading…</div>
        ) : rows.length === 0 ? (
          <EmptyState
            title="No users found"
            description="Invite staff from Invitation Management, then manage them here."
          />
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User</TableHead>
                  <TableHead>Roles</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Verified</TableHead>
                  <TableHead>Last login</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((user) => {
                  const isSelf = sessionUser?.id === user.id;
                  const actions = lifecycleActionsFor(user.status);
                  return (
                    <TableRow key={user.id}>
                      <TableCell>
                        <div className="font-medium">
                          {formatPersonName(user.firstName, user.lastName)}
                        </div>
                        <div className="text-muted-foreground text-xs">{user.email}</div>
                      </TableCell>
                      <TableCell>
                        <div className="flex max-w-56 flex-wrap gap-1">
                          {user.roles.map((role) => (
                            <span
                              key={role}
                              className="bg-muted rounded-md px-1.5 py-0.5 text-[11px]"
                            >
                              {formatEnumLabel(role)}
                            </span>
                          ))}
                        </div>
                      </TableCell>
                      <TableCell>
                        <StatusBadge status={user.status} />
                      </TableCell>
                      <TableCell>{user.emailVerified ? 'Yes' : 'No'}</TableCell>
                      <TableCell className="text-muted-foreground text-xs">
                        {user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : 'Never'}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-1">
                          <Can permissions={[Permissions.USER_UPDATE]}>
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon-sm"
                              onClick={() => setEditing(user)}
                              aria-label="Edit user"
                            >
                              <PencilIcon />
                            </Button>
                          </Can>
                          <Can permissions={[Permissions.USER_UPDATE]}>
                            {!isSelf && actions.length > 0 ? (
                              <DropdownMenu>
                                <DropdownMenuTrigger
                                  render={
                                    <Button
                                      type="button"
                                      variant="ghost"
                                      size="icon-sm"
                                      aria-label="Lifecycle actions"
                                    >
                                      <MoreHorizontalIcon />
                                    </Button>
                                  }
                                />
                                <DropdownMenuContent align="end">
                                  {actions.map((item) => (
                                    <DropdownMenuItem
                                      key={item.action}
                                      disabled={isBusy}
                                      onClick={() => void runLifecycle(user, item.action)}
                                    >
                                      {item.label}
                                    </DropdownMenuItem>
                                  ))}
                                </DropdownMenuContent>
                              </DropdownMenu>
                            ) : null}
                          </Can>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </div>
        )}

        <PaginationControls
          page={ui.page}
          size={ui.size}
          totalPages={usersQuery.data?.totalPages ?? 0}
          totalElements={usersQuery.data?.totalElements ?? 0}
          onPageChange={(page) => dispatch(setListPage({ key: 'users', page }))}
          disabled={usersQuery.isFetching}
        />
      </div>

      <Dialog open={Boolean(editing)} onOpenChange={(open) => !open && setEditing(null)}>
        <DialogContent showCloseButton>
          <DialogHeader>
            <DialogTitle>Edit user profile</DialogTitle>
            <DialogDescription>
              Update display name and phone. Email and roles are managed separately.
            </DialogDescription>
          </DialogHeader>
          <form className="grid gap-4" onSubmit={onSubmit} noValidate>
            <div className="space-y-2">
              <Label htmlFor="firstName">First name</Label>
              <Input id="firstName" {...form.register('firstName')} />
              {form.formState.errors.firstName ? (
                <p className="text-destructive text-xs">
                  {form.formState.errors.firstName.message}
                </p>
              ) : null}
            </div>
            <div className="space-y-2">
              <Label htmlFor="lastName">Last name</Label>
              <Input id="lastName" {...form.register('lastName')} />
              {form.formState.errors.lastName ? (
                <p className="text-destructive text-xs">{form.formState.errors.lastName.message}</p>
              ) : null}
            </div>
            <div className="space-y-2">
              <Label htmlFor="phone">Phone</Label>
              <Input id="phone" {...form.register('phone')} />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setEditing(null)}>
                Cancel
              </Button>
              <Button type="submit" disabled={isBusy}>
                {mutations.update.isPending ? (
                  <Loader2Icon className="animate-spin" data-icon="inline-start" />
                ) : null}
                Save changes
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
