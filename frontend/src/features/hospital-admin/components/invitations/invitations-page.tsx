'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, PlusIcon, RotateCcwIcon, XIcon } from 'lucide-react';
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
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Textarea } from '@/components/ui/textarea';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { FilterSelect } from '@/features/hospital-admin/components/shared/filter-select';
import { ListToolbar } from '@/features/hospital-admin/components/shared/list-toolbar';
import { PaginationControls } from '@/features/hospital-admin/components/shared/pagination-controls';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import {
  useInvitationMutations,
  useInvitationsQuery,
} from '@/features/hospital-admin/hooks/use-invitations';
import {
  resetListFilters,
  selectInvitationsUi,
  setInvitationsEmail,
  setListPage,
  setListStatus,
} from '@/features/hospital-admin/store/hospital-admin-ui-slice';
import {
  INVITABLE_ROLE_TYPES,
  INVITATION_STATUSES,
  type InvitationStatus,
} from '@/features/hospital-admin/types/enums';
import type { UserInvitationResponse } from '@/features/hospital-admin/types/invitation';
import {
  createInvitationSchema,
  type CreateInvitationFormValues,
} from '@/features/hospital-admin/validation/invitation-schema';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel, formatPersonName } from '@/lib/page-query';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

const emptyForm: CreateInvitationFormValues = {
  email: '',
  firstName: '',
  lastName: '',
  roleType: 'DOCTOR',
  message: '',
};

export function InvitationsPage() {
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectInvitationsUi);
  const [dialogOpen, setDialogOpen] = useState(false);

  const query = useMemo(
    () => ({
      email: ui.email.trim() || undefined,
      status: (ui.status || undefined) as InvitationStatus | undefined,
      page: ui.page,
      size: ui.size,
      sort: 'createdAt,desc',
    }),
    [ui],
  );

  const invitationsQuery = useInvitationsQuery(query);
  const mutations = useInvitationMutations();

  const form = useForm<CreateInvitationFormValues>({
    resolver: zodResolver(createInvitationSchema),
    defaultValues: emptyForm,
  });

  useEffect(() => {
    if (dialogOpen) {
      form.reset(emptyForm);
    }
  }, [dialogOpen, form]);

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      await mutations.create.mutateAsync({
        email: values.email.trim().toLowerCase(),
        firstName: values.firstName?.trim() || null,
        lastName: values.lastName?.trim() || null,
        roleType: values.roleType,
        message: values.message?.trim() || null,
      });
      toast.success('Invitation sent');
      setDialogOpen(false);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to send invitation'));
    }
  });

  const resend = async (invitation: UserInvitationResponse) => {
    try {
      await mutations.resend.mutateAsync(invitation.id);
      toast.success('Invitation resent');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to resend invitation'));
    }
  };

  const cancel = async (invitation: UserInvitationResponse) => {
    try {
      await mutations.cancel.mutateAsync(invitation.id);
      toast.success('Invitation cancelled');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to cancel invitation'));
    }
  };

  const rows = invitationsQuery.data?.content ?? [];
  const isBusy =
    mutations.create.isPending || mutations.resend.isPending || mutations.cancel.isPending;

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="Invitation management"
        description="Invite hospital staff by email. Recipients accept with a secure token to create their account."
        actions={
          <Can permissions={[Permissions.USER_CREATE]}>
            <Button type="button" onClick={() => setDialogOpen(true)}>
              <PlusIcon data-icon="inline-start" />
              Invite user
            </Button>
          </Can>
        }
      />

      <div className="rounded-xl border bg-card">
        <div className="border-b p-4">
          <ListToolbar
            search={ui.email}
            onSearchChange={(email) => dispatch(setInvitationsEmail(email))}
            searchPlaceholder="Filter by email…"
            showReset={Boolean(ui.email || ui.status)}
            onReset={() => dispatch(resetListFilters('invitations'))}
            filters={
              <FilterSelect
                value={ui.status}
                onValueChange={(status) => dispatch(setListStatus({ key: 'invitations', status }))}
                options={INVITATION_STATUSES}
                placeholder="Status"
                allLabel="All statuses"
              />
            }
          />
        </div>

        {invitationsQuery.isError ? (
          <EmptyState
            title="Unable to load invitations"
            description={getErrorMessage(invitationsQuery.error)}
          />
        ) : invitationsQuery.isLoading ? (
          <div className="text-muted-foreground px-6 py-16 text-center text-sm">Loading…</div>
        ) : rows.length === 0 ? (
          <EmptyState
            title="No invitations"
            description="Send an invitation to onboard doctors, nurses, and other hospital staff."
            action={
              <Can permissions={[Permissions.USER_CREATE]}>
                <Button type="button" variant="outline" onClick={() => setDialogOpen(true)}>
                  Invite user
                </Button>
              </Can>
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Invitee</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Expires</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((invitation) => {
                  const isPending = invitation.status === 'PENDING' && !invitation.expired;
                  return (
                    <TableRow key={invitation.id}>
                      <TableCell>
                        <div className="font-medium">
                          {formatPersonName(invitation.firstName, invitation.lastName) ||
                            invitation.email}
                        </div>
                        <div className="text-muted-foreground text-xs">{invitation.email}</div>
                      </TableCell>
                      <TableCell>{formatEnumLabel(invitation.roleType)}</TableCell>
                      <TableCell>
                        <StatusBadge status={invitation.expired ? 'EXPIRED' : invitation.status} />
                      </TableCell>
                      <TableCell className="text-muted-foreground text-xs">
                        {new Date(invitation.expiresAt).toLocaleString()}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-1">
                          {isPending ? (
                            <>
                              <Can permissions={[Permissions.USER_CREATE]}>
                                <Button
                                  type="button"
                                  variant="ghost"
                                  size="icon-sm"
                                  disabled={isBusy}
                                  onClick={() => void resend(invitation)}
                                  aria-label="Resend invitation"
                                >
                                  <RotateCcwIcon />
                                </Button>
                              </Can>
                              <Can permissions={[Permissions.USER_UPDATE]}>
                                <Button
                                  type="button"
                                  variant="ghost"
                                  size="icon-sm"
                                  disabled={isBusy}
                                  onClick={() => void cancel(invitation)}
                                  aria-label="Cancel invitation"
                                >
                                  <XIcon />
                                </Button>
                              </Can>
                            </>
                          ) : null}
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
          totalPages={invitationsQuery.data?.totalPages ?? 0}
          totalElements={invitationsQuery.data?.totalElements ?? 0}
          onPageChange={(page) => dispatch(setListPage({ key: 'invitations', page }))}
          disabled={invitationsQuery.isFetching}
        />
      </div>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-lg" showCloseButton>
          <DialogHeader>
            <DialogTitle>Invite user</DialogTitle>
            <DialogDescription>
              An email with a secure invitation link will be sent to the recipient.
            </DialogDescription>
          </DialogHeader>
          <form className="grid gap-4" onSubmit={onSubmit} noValidate>
            <div className="space-y-2">
              <Label htmlFor="invite-email">Email</Label>
              <Input id="invite-email" type="email" {...form.register('email')} />
              {form.formState.errors.email ? (
                <p className="text-destructive text-xs">{form.formState.errors.email.message}</p>
              ) : null}
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="invite-first">First name</Label>
                <Input id="invite-first" {...form.register('firstName')} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="invite-last">Last name</Label>
                <Input id="invite-last" {...form.register('lastName')} />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Role</Label>
              <Select
                value={form.watch('roleType')}
                onValueChange={(value) =>
                  form.setValue('roleType', value as CreateInvitationFormValues['roleType'], {
                    shouldValidate: true,
                  })
                }
              >
                <SelectTrigger className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {INVITABLE_ROLE_TYPES.map((role) => (
                    <SelectItem key={role} value={role}>
                      {formatEnumLabel(role)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="invite-message">Message</Label>
              <Textarea id="invite-message" rows={3} {...form.register('message')} />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={isBusy}>
                {mutations.create.isPending ? (
                  <Loader2Icon className="animate-spin" data-icon="inline-start" />
                ) : null}
                Send invitation
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
