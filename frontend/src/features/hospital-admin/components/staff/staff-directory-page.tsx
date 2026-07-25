'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, PencilIcon, PlusIcon, Trash2Icon } from 'lucide-react';
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
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Permissions } from '@/features/authorization/constants/permissions';
import { useAuthorization } from '@/features/authorization/hooks/use-authorization';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { FilterSelect } from '@/features/hospital-admin/components/shared/filter-select';
import { ListToolbar } from '@/features/hospital-admin/components/shared/list-toolbar';
import { PaginationControls } from '@/features/hospital-admin/components/shared/pagination-controls';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { useDepartmentsQuery } from '@/features/hospital-admin/hooks/use-departments';
import { useStaffMutations, useStaffQuery } from '@/features/hospital-admin/hooks/use-staff';
import { useUsersQuery } from '@/features/hospital-admin/hooks/use-users';
import {
  resetListFilters,
  selectStaffUi,
  setListPage,
  setListSearch,
  setListStatus,
  setStaffDepartmentId,
  setStaffType,
} from '@/features/hospital-admin/store/hospital-admin-ui-slice';
import type {
  EmploymentStatus,
  EmploymentType,
  StaffShift,
  StaffType,
} from '@/features/hospital-admin/types/enums';
import {
  EMPLOYMENT_STATUSES,
  EMPLOYMENT_TYPES,
  STAFF_SHIFTS,
  STAFF_TYPES,
  type RoleType,
} from '@/features/hospital-admin/types/enums';
import {
  STAFF_RESOURCE_LABEL,
  type StaffProfile,
} from '@/features/hospital-admin/types/staff';
import {
  staffFormSchema,
  type StaffFormValues,
} from '@/features/hospital-admin/validation/staff-schema';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel, formatPersonName } from '@/lib/page-query';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

const emptyForm = (staffType: StaffType): StaffFormValues => ({
  staffType,
  userId: '',
  departmentId: '',
  employeeCode: '',
  jobTitle: '',
  employmentStatus: 'ACTIVE',
  employmentType: 'FULL_TIME',
  hiredAt: '',
  terminatedAt: '',
  specialization: '',
  licenseNumber: '',
  qualification: '',
  experienceYears: '',
  consultationFee: '',
  shift: 'MORNING',
  deskLocation: '',
  languages: '',
  specialtyArea: '',
  certification: '',
  pharmacyLocation: '',
});

function staffTypeToRoleType(staffType: StaffType): RoleType {
  switch (staffType) {
    case 'DOCTOR':
      return 'DOCTOR';
    case 'NURSE':
      return 'NURSE';
    case 'RECEPTIONIST':
      return 'RECEPTIONIST';
    case 'LABORATORY_STAFF':
      return 'LAB_TECHNICIAN';
    case 'PHARMACIST':
      return 'PHARMACIST';
  }
}

function emptyToNull(value?: string) {
  const trimmed = value?.trim();
  return trimmed ? trimmed : null;
}

function toStaffPayload(values: StaffFormValues, existing?: StaffProfile | null) {
  const base = {
    userId: values.userId,
    departmentId: values.departmentId,
    employeeCode: values.employeeCode.trim(),
    jobTitle: emptyToNull(values.jobTitle),
    employmentStatus: values.employmentStatus,
    employmentType: values.employmentType,
    hiredAt: emptyToNull(values.hiredAt),
    terminatedAt: emptyToNull(values.terminatedAt),
    reportsToStaffId: existing?.reportsToStaffId ?? null,
  };

  switch (values.staffType) {
    case 'DOCTOR':
      return {
        ...base,
        specialization: values.specialization!.trim(),
        licenseNumber: values.licenseNumber!.trim(),
        qualification: emptyToNull(values.qualification),
        experienceYears: values.experienceYears?.trim()
          ? Number(values.experienceYears)
          : null,
        consultationFee: values.consultationFee?.trim()
          ? Number(values.consultationFee)
          : null,
      };
    case 'NURSE':
      return {
        ...base,
        shift: values.shift as StaffShift,
        qualification: emptyToNull(values.qualification),
        licenseNumber: emptyToNull(values.licenseNumber),
      };
    case 'RECEPTIONIST':
      return {
        ...base,
        deskLocation: emptyToNull(values.deskLocation),
        languages: emptyToNull(values.languages),
      };
    case 'LABORATORY_STAFF':
      return {
        ...base,
        specialtyArea: emptyToNull(values.specialtyArea),
        licenseNumber: emptyToNull(values.licenseNumber),
        certification: emptyToNull(values.certification),
      };
    case 'PHARMACIST':
      return {
        ...base,
        licenseNumber: values.licenseNumber!.trim(),
        pharmacyLocation: emptyToNull(values.pharmacyLocation),
        qualification: emptyToNull(values.qualification),
      };
  }
}

function profileToForm(staffType: StaffType, profile: StaffProfile): StaffFormValues {
  const base = emptyForm(staffType);
  return {
    ...base,
    userId: profile.userId,
    departmentId: profile.departmentId,
    employeeCode: profile.employeeCode,
    jobTitle: profile.jobTitle ?? '',
    employmentStatus: profile.employmentStatus,
    employmentType: profile.employmentType,
    hiredAt: profile.hiredAt ?? '',
    terminatedAt: profile.terminatedAt ?? '',
    ...('specialization' in profile
      ? {
          specialization: profile.specialization,
          licenseNumber: profile.licenseNumber,
          qualification: profile.qualification ?? '',
          experienceYears:
            profile.experienceYears !== null && profile.experienceYears !== undefined
              ? String(profile.experienceYears)
              : '',
          consultationFee:
            profile.consultationFee !== null && profile.consultationFee !== undefined
              ? String(profile.consultationFee)
              : '',
        }
      : {}),
    ...('shift' in profile
      ? {
          shift: profile.shift,
          qualification: profile.qualification ?? '',
          licenseNumber: profile.licenseNumber ?? '',
        }
      : {}),
    ...('deskLocation' in profile
      ? {
          deskLocation: profile.deskLocation ?? '',
          languages: profile.languages ?? '',
        }
      : {}),
    ...('specialtyArea' in profile
      ? {
          specialtyArea: profile.specialtyArea ?? '',
          licenseNumber: profile.licenseNumber ?? '',
          certification: profile.certification ?? '',
        }
      : {}),
    ...('pharmacyLocation' in profile
      ? {
          licenseNumber: profile.licenseNumber,
          pharmacyLocation: profile.pharmacyLocation ?? '',
          qualification: profile.qualification ?? '',
        }
      : {}),
  };
}

export function StaffDirectoryPage() {
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectStaffUi);
  const { can } = useAuthorization();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<StaffProfile | null>(null);
  const [deleting, setDeleting] = useState<StaffProfile | null>(null);

  const canCreate =
    ui.staffType === 'DOCTOR'
      ? can(Permissions.DOCTOR_CREATE)
      : can(Permissions.STAFF_CREATE);
  const canUpdate =
    ui.staffType === 'DOCTOR'
      ? can(Permissions.DOCTOR_UPDATE)
      : can(Permissions.STAFF_UPDATE);
  const canDelete =
    ui.staffType === 'DOCTOR'
      ? can(Permissions.DOCTOR_DELETE)
      : can(Permissions.STAFF_DELETE);
  const canRead =
    ui.staffType === 'DOCTOR' ? can(Permissions.DOCTOR_READ) : can(Permissions.STAFF_READ);
  const canReadDepartments = can(Permissions.DEPARTMENT_READ);

  const allowedStaffTypes = useMemo(
    () =>
      STAFF_TYPES.filter((type) =>
        type === 'DOCTOR' ? can(Permissions.DOCTOR_READ) : can(Permissions.STAFF_READ),
      ),
    [can],
  );

  useEffect(() => {
    if (allowedStaffTypes.length === 0) {
      return;
    }
    if (!allowedStaffTypes.includes(ui.staffType)) {
      dispatch(setStaffType(allowedStaffTypes[0]!));
    }
  }, [allowedStaffTypes, dispatch, ui.staffType]);

  const query = useMemo(
    () => ({
      q: ui.q.trim() || undefined,
      employmentStatus: (ui.status || undefined) as EmploymentStatus | undefined,
      departmentId: ui.departmentId || undefined,
      page: ui.page,
      size: ui.size,
      sort: 'employeeCode,asc',
    }),
    [ui],
  );

  const staffQuery = useStaffQuery(ui.staffType, query, canRead);
  const mutations = useStaffMutations(ui.staffType);
  const departmentsQuery = useDepartmentsQuery(
    { page: 0, size: 100, sort: 'name,asc', status: 'ACTIVE' },
    canReadDepartments,
  );
  const usersQuery = useUsersQuery(
    {
      page: 0,
      size: 100,
      status: 'ACTIVE',
      roleType: staffTypeToRoleType(ui.staffType),
      sort: 'lastName,asc',
    },
    canCreate || canUpdate,
  );

  const form = useForm<StaffFormValues>({
    resolver: zodResolver(staffFormSchema),
    defaultValues: emptyForm(ui.staffType),
  });

  useEffect(() => {
    if (!dialogOpen) {
      return;
    }
    form.reset(editing ? profileToForm(ui.staffType, editing) : emptyForm(ui.staffType));
  }, [dialogOpen, editing, form, ui.staffType]);

  const departmentNameById = useMemo(() => {
    const map = new Map<string, string>();
    for (const department of departmentsQuery.data?.content ?? []) {
      map.set(department.id, department.name);
    }
    return map;
  }, [departmentsQuery.data]);

  const userLabelById = useMemo(() => {
    const map = new Map<string, string>();
    for (const user of usersQuery.data?.content ?? []) {
      map.set(user.id, `${formatPersonName(user.firstName, user.lastName)} (${user.email})`);
    }
    return map;
  }, [usersQuery.data]);

  const openCreate = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  const openEdit = (profile: StaffProfile) => {
    setEditing(profile);
    setDialogOpen(true);
  };

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      const payload = toStaffPayload(values, editing);
      if (editing) {
        await mutations.update.mutateAsync({ id: editing.id, payload });
        toast.success('Staff profile updated');
      } else {
        await mutations.create.mutateAsync(payload);
        toast.success('Staff profile created');
      }
      setDialogOpen(false);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save staff profile'));
    }
  });

  const onDelete = async () => {
    if (!deleting) {
      return;
    }
    try {
      await mutations.remove.mutateAsync(deleting.id);
      toast.success('Staff profile removed');
      setDeleting(null);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to remove staff profile'));
    }
  };

  const rows = staffQuery.data?.content ?? [];
  const isBusy =
    mutations.create.isPending || mutations.update.isPending || mutations.remove.isPending;
  const watchedType = ui.staffType;

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="Staff directory"
        description="Manage employment profiles for doctors, nurses, and support staff linked to hospital users."
        actions={
          canCreate ? (
            <Button type="button" onClick={openCreate}>
              <PlusIcon data-icon="inline-start" />
              Add staff
            </Button>
          ) : null
        }
      />

      <Tabs
        value={ui.staffType}
        onValueChange={(value) => dispatch(setStaffType(value as StaffType))}
      >
        <TabsList className="h-auto w-full flex-wrap justify-start gap-1">
          {STAFF_TYPES.map((type) => {
            const allowed =
              type === 'DOCTOR' ? can(Permissions.DOCTOR_READ) : can(Permissions.STAFF_READ);
            if (!allowed) {
              return null;
            }
            return (
              <TabsTrigger key={type} value={type}>
                {STAFF_RESOURCE_LABEL[type]}
              </TabsTrigger>
            );
          })}
        </TabsList>
      </Tabs>

      <div className="rounded-xl border bg-card">
        <div className="border-b p-4">
          <ListToolbar
            search={ui.q}
            onSearchChange={(q) => dispatch(setListSearch({ key: 'staff', q }))}
            searchPlaceholder="Search employee code, title, license…"
            showReset={Boolean(ui.q || ui.status || ui.departmentId)}
            onReset={() => dispatch(resetListFilters('staff'))}
            filters={
              <>
                <FilterSelect
                  value={ui.status}
                  onValueChange={(status) => dispatch(setListStatus({ key: 'staff', status }))}
                  options={EMPLOYMENT_STATUSES}
                  placeholder="Employment status"
                  allLabel="All statuses"
                />
                <FilterSelect
                  value={ui.departmentId}
                  onValueChange={(departmentId) => dispatch(setStaffDepartmentId(departmentId))}
                  options={(departmentsQuery.data?.content ?? []).map((d) => d.id)}
                  labels={Object.fromEntries(
                    (departmentsQuery.data?.content ?? []).map((d) => [d.id, d.name]),
                  )}
                  placeholder="Department"
                  allLabel="All departments"
                  className="min-w-48"
                />
              </>
            }
          />
        </div>

        {!canRead ? (
          <EmptyState
            title="No access"
            description="You do not have permission to view this staff category."
          />
        ) : staffQuery.isError ? (
          <EmptyState
            title="Unable to load staff"
            description={getErrorMessage(staffQuery.error)}
          />
        ) : staffQuery.isLoading ? (
          <div className="text-muted-foreground px-6 py-16 text-center text-sm">Loading…</div>
        ) : rows.length === 0 ? (
          <EmptyState
            title="No staff profiles"
            description="Invite a user first, then create their employment profile here."
            action={
              canCreate ? (
                <Button type="button" variant="outline" onClick={openCreate}>
                  Add staff
                </Button>
              ) : null
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Employee</TableHead>
                  <TableHead>Department</TableHead>
                  <TableHead>Title</TableHead>
                  <TableHead>Employment</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((profile) => (
                  <TableRow key={profile.id}>
                    <TableCell>
                      <div className="font-medium font-mono text-xs">{profile.employeeCode}</div>
                      <div className="text-muted-foreground text-xs">
                        {userLabelById.get(profile.userId) ?? profile.userId.slice(0, 8)}
                      </div>
                    </TableCell>
                    <TableCell>
                      {departmentNameById.get(profile.departmentId) ?? '—'}
                    </TableCell>
                    <TableCell>{profile.jobTitle || '—'}</TableCell>
                    <TableCell>
                      <StatusBadge status={profile.employmentStatus} />
                    </TableCell>
                    <TableCell>{formatEnumLabel(profile.employmentType)}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-1">
                        {canUpdate ? (
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon-sm"
                            onClick={() => openEdit(profile)}
                            aria-label="Edit staff"
                          >
                            <PencilIcon />
                          </Button>
                        ) : null}
                        {canDelete ? (
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon-sm"
                            onClick={() => setDeleting(profile)}
                            aria-label="Delete staff"
                          >
                            <Trash2Icon />
                          </Button>
                        ) : null}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}

        <PaginationControls
          page={ui.page}
          size={ui.size}
          totalPages={staffQuery.data?.totalPages ?? 0}
          totalElements={staffQuery.data?.totalElements ?? 0}
          onPageChange={(page) => dispatch(setListPage({ key: 'staff', page }))}
          disabled={staffQuery.isFetching}
        />
      </div>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl" showCloseButton>
          <DialogHeader>
            <DialogTitle>{editing ? 'Edit staff profile' : 'Add staff profile'}</DialogTitle>
            <DialogDescription>
              Link an existing hospital user to a department employment record.
            </DialogDescription>
          </DialogHeader>
          <form className="grid gap-4" onSubmit={onSubmit} noValidate>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2 sm:col-span-2">
                <Label>User</Label>
                <Select
                  value={form.watch('userId') || undefined}
                  onValueChange={(value) =>
                    form.setValue('userId', value ?? '', { shouldValidate: true })
                  }
                  disabled={Boolean(editing)}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="Select user" />
                  </SelectTrigger>
                  <SelectContent>
                    {(usersQuery.data?.content ?? []).map((user) => (
                      <SelectItem key={user.id} value={user.id}>
                        {formatPersonName(user.firstName, user.lastName)} · {user.email}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {form.formState.errors.userId ? (
                  <p className="text-destructive text-xs">{form.formState.errors.userId.message}</p>
                ) : null}
              </div>

              <div className="space-y-2 sm:col-span-2">
                <Label>Department</Label>
                <Select
                  value={form.watch('departmentId') || undefined}
                  onValueChange={(value) =>
                    form.setValue('departmentId', value ?? '', { shouldValidate: true })
                  }
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="Select department" />
                  </SelectTrigger>
                  <SelectContent>
                    {(departmentsQuery.data?.content ?? []).map((department) => (
                      <SelectItem key={department.id} value={department.id}>
                        {department.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {form.formState.errors.departmentId ? (
                  <p className="text-destructive text-xs">
                    {form.formState.errors.departmentId.message}
                  </p>
                ) : null}
              </div>

              <div className="space-y-2">
                <Label htmlFor="employeeCode">Employee code</Label>
                <Input id="employeeCode" {...form.register('employeeCode')} />
                {form.formState.errors.employeeCode ? (
                  <p className="text-destructive text-xs">
                    {form.formState.errors.employeeCode.message}
                  </p>
                ) : null}
              </div>
              <div className="space-y-2">
                <Label htmlFor="jobTitle">Job title</Label>
                <Input id="jobTitle" {...form.register('jobTitle')} />
              </div>

              <div className="space-y-2">
                <Label>Employment status</Label>
                <Select
                  value={form.watch('employmentStatus')}
                  onValueChange={(value) =>
                    form.setValue('employmentStatus', value as EmploymentStatus, {
                      shouldValidate: true,
                    })
                  }
                >
                  <SelectTrigger className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {EMPLOYMENT_STATUSES.map((status) => (
                      <SelectItem key={status} value={status}>
                        {formatEnumLabel(status)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Employment type</Label>
                <Select
                  value={form.watch('employmentType')}
                  onValueChange={(value) =>
                    form.setValue('employmentType', value as EmploymentType, {
                      shouldValidate: true,
                    })
                  }
                >
                  <SelectTrigger className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {EMPLOYMENT_TYPES.map((type) => (
                      <SelectItem key={type} value={type}>
                        {formatEnumLabel(type)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {watchedType === 'DOCTOR' ? (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="specialization">Specialization</Label>
                    <Input id="specialization" {...form.register('specialization')} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="licenseNumber">License number</Label>
                    <Input id="licenseNumber" {...form.register('licenseNumber')} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="qualification">Qualification</Label>
                    <Input id="qualification" {...form.register('qualification')} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="experienceYears">Experience (years)</Label>
                    <Input id="experienceYears" {...form.register('experienceYears')} />
                  </div>
                </>
              ) : null}

              {watchedType === 'NURSE' ? (
                <>
                  <div className="space-y-2">
                    <Label>Shift</Label>
                    <Select
                      value={form.watch('shift')}
                      onValueChange={(value) =>
                        form.setValue('shift', value as StaffShift, { shouldValidate: true })
                      }
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {STAFF_SHIFTS.map((shift) => (
                          <SelectItem key={shift} value={shift}>
                            {formatEnumLabel(shift)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="nurse-license">License number</Label>
                    <Input id="nurse-license" {...form.register('licenseNumber')} />
                  </div>
                </>
              ) : null}

              {watchedType === 'RECEPTIONIST' ? (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="deskLocation">Desk location</Label>
                    <Input id="deskLocation" {...form.register('deskLocation')} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="languages">Languages</Label>
                    <Input id="languages" {...form.register('languages')} />
                  </div>
                </>
              ) : null}

              {watchedType === 'LABORATORY_STAFF' ? (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="specialtyArea">Specialty area</Label>
                    <Input id="specialtyArea" {...form.register('specialtyArea')} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="lab-license">License number</Label>
                    <Input id="lab-license" {...form.register('licenseNumber')} />
                  </div>
                </>
              ) : null}

              {watchedType === 'PHARMACIST' ? (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="pharm-license">License number</Label>
                    <Input id="pharm-license" {...form.register('licenseNumber')} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="pharmacyLocation">Pharmacy location</Label>
                    <Input id="pharmacyLocation" {...form.register('pharmacyLocation')} />
                  </div>
                </>
              ) : null}
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={isBusy}>
                {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
                {editing ? 'Save changes' : 'Create profile'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(deleting)} onOpenChange={(open) => !open && setDeleting(null)}>
        <DialogContent showCloseButton>
          <DialogHeader>
            <DialogTitle>Remove staff profile</DialogTitle>
            <DialogDescription>
              Soft-delete employee <span className="font-mono">{deleting?.employeeCode}</span>? The
              linked user account is not deleted.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setDeleting(null)}>
              Cancel
            </Button>
            <Button type="button" variant="destructive" disabled={isBusy} onClick={onDelete}>
              Remove
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
