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
  useDepartmentMutation,
  useDepartmentsQuery,
} from '@/features/hospital-admin/hooks/use-departments';
import {
  resetListFilters,
  selectDepartmentsUi,
  setListPage,
  setListSearch,
  setListStatus,
} from '@/features/hospital-admin/store/hospital-admin-ui-slice';
import type { DepartmentResponse } from '@/features/hospital-admin/types/department';
import {
  DEPARTMENT_STATUSES,
  DEPARTMENT_TYPES,
  type DepartmentStatus,
  type DepartmentType,
} from '@/features/hospital-admin/types/enums';
import {
  departmentFormSchema,
  type DepartmentFormValues,
} from '@/features/hospital-admin/validation/department-schema';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

const emptyForm: DepartmentFormValues = {
  name: '',
  code: '',
  description: '',
  departmentType: 'CLINICAL',
  status: 'ACTIVE',
  location: '',
};

function toPayload(values: DepartmentFormValues) {
  return {
    name: values.name.trim(),
    code: values.code.trim().toUpperCase(),
    description: values.description?.trim() || null,
    departmentType: values.departmentType,
    status: values.status,
    location: values.location?.trim() || null,
  };
}

export function DepartmentsPage() {
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectDepartmentsUi);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<DepartmentResponse | null>(null);
  const [deleting, setDeleting] = useState<DepartmentResponse | null>(null);

  const query = useMemo(
    () => ({
      q: ui.q.trim() || undefined,
      status: (ui.status || undefined) as DepartmentStatus | undefined,
      page: ui.page,
      size: ui.size,
      sort: 'name,asc',
    }),
    [ui],
  );

  const departmentsQuery = useDepartmentsQuery(query);
  const mutations = useDepartmentMutation();

  const form = useForm<DepartmentFormValues>({
    resolver: zodResolver(departmentFormSchema),
    defaultValues: emptyForm,
  });

  useEffect(() => {
    if (!dialogOpen) {
      return;
    }
    form.reset(
      editing
        ? {
            name: editing.name,
            code: editing.code,
            description: editing.description ?? '',
            departmentType: editing.departmentType,
            status: editing.status,
            location: editing.location ?? '',
          }
        : emptyForm,
    );
  }, [dialogOpen, editing, form]);

  const openCreate = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  const openEdit = (department: DepartmentResponse) => {
    setEditing(department);
    setDialogOpen(true);
  };

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      const payload = toPayload(values);
      if (editing) {
        await mutations.update.mutateAsync({ id: editing.id, payload });
        toast.success('Department updated');
      } else {
        await mutations.create.mutateAsync(payload);
        toast.success('Department created');
      }
      setDialogOpen(false);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save department'));
    }
  });

  const onDelete = async () => {
    if (!deleting) {
      return;
    }
    try {
      await mutations.remove.mutateAsync(deleting.id);
      toast.success('Department deleted');
      setDeleting(null);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to delete department'));
    }
  };

  const rows = departmentsQuery.data?.content ?? [];
  const isBusy =
    mutations.create.isPending || mutations.update.isPending || mutations.remove.isPending;

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="Departments"
        description="Organize clinical and support units for your hospital. Codes must be unique within the tenant."
        actions={
          <Can permissions={[Permissions.DEPARTMENT_CREATE]}>
            <Button type="button" onClick={openCreate}>
              <PlusIcon data-icon="inline-start" />
              Add department
            </Button>
          </Can>
        }
      />

      <div className="rounded-xl border bg-card">
        <div className="border-b p-4">
          <ListToolbar
            search={ui.q}
            onSearchChange={(q) => dispatch(setListSearch({ key: 'departments', q }))}
            searchPlaceholder="Search name, code, location…"
            showReset={Boolean(ui.q || ui.status)}
            onReset={() => dispatch(resetListFilters('departments'))}
            filters={
              <FilterSelect
                value={ui.status}
                onValueChange={(status) =>
                  dispatch(setListStatus({ key: 'departments', status }))
                }
                options={DEPARTMENT_STATUSES}
                placeholder="Status"
                allLabel="All statuses"
              />
            }
          />
        </div>

        {departmentsQuery.isError ? (
          <EmptyState
            title="Unable to load departments"
            description={getErrorMessage(departmentsQuery.error)}
          />
        ) : departmentsQuery.isLoading ? (
          <div className="text-muted-foreground px-6 py-16 text-center text-sm">Loading…</div>
        ) : rows.length === 0 ? (
          <EmptyState
            title="No departments found"
            description="Create your first department or adjust search filters."
            action={
              <Can permissions={[Permissions.DEPARTMENT_CREATE]}>
                <Button type="button" variant="outline" onClick={openCreate}>
                  Add department
                </Button>
              </Can>
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Code</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Location</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((department) => (
                  <TableRow key={department.id}>
                    <TableCell className="font-medium">{department.name}</TableCell>
                    <TableCell className="font-mono text-xs">{department.code}</TableCell>
                    <TableCell>{formatEnumLabel(department.departmentType)}</TableCell>
                    <TableCell>
                      <StatusBadge status={department.status} />
                    </TableCell>
                    <TableCell>{department.location || '—'}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-1">
                        <Can permissions={[Permissions.DEPARTMENT_UPDATE]}>
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon-sm"
                            onClick={() => openEdit(department)}
                            aria-label={`Edit ${department.name}`}
                          >
                            <PencilIcon />
                          </Button>
                        </Can>
                        <Can permissions={[Permissions.DEPARTMENT_DELETE]}>
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon-sm"
                            onClick={() => setDeleting(department)}
                            aria-label={`Delete ${department.name}`}
                          >
                            <Trash2Icon />
                          </Button>
                        </Can>
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
          totalPages={departmentsQuery.data?.totalPages ?? 0}
          totalElements={departmentsQuery.data?.totalElements ?? 0}
          onPageChange={(page) => dispatch(setListPage({ key: 'departments', page }))}
          disabled={departmentsQuery.isFetching}
        />
      </div>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-lg" showCloseButton>
          <DialogHeader>
            <DialogTitle>{editing ? 'Edit department' : 'Add department'}</DialogTitle>
            <DialogDescription>
              Departments group staff and clinical workflows within this hospital tenant.
            </DialogDescription>
          </DialogHeader>
          <form className="grid gap-4" onSubmit={onSubmit} noValidate>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="dept-name">Name</Label>
                <Input id="dept-name" {...form.register('name')} />
                {form.formState.errors.name ? (
                  <p className="text-destructive text-xs">{form.formState.errors.name.message}</p>
                ) : null}
              </div>
              <div className="space-y-2">
                <Label htmlFor="dept-code">Code</Label>
                <Input id="dept-code" {...form.register('code')} />
                {form.formState.errors.code ? (
                  <p className="text-destructive text-xs">{form.formState.errors.code.message}</p>
                ) : null}
              </div>
              <div className="space-y-2">
                <Label>Status</Label>
                <Select
                  value={form.watch('status')}
                  onValueChange={(value) =>
                    form.setValue('status', value as DepartmentStatus, { shouldValidate: true })
                  }
                >
                  <SelectTrigger className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {DEPARTMENT_STATUSES.map((status) => (
                      <SelectItem key={status} value={status}>
                        {formatEnumLabel(status)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2 sm:col-span-2">
                <Label>Type</Label>
                <Select
                  value={form.watch('departmentType')}
                  onValueChange={(value) =>
                    form.setValue('departmentType', value as DepartmentType, {
                      shouldValidate: true,
                    })
                  }
                >
                  <SelectTrigger className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {DEPARTMENT_TYPES.map((type) => (
                      <SelectItem key={type} value={type}>
                        {formatEnumLabel(type)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="dept-location">Location</Label>
                <Input id="dept-location" {...form.register('location')} />
              </div>
              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="dept-description">Description</Label>
                <Textarea id="dept-description" rows={3} {...form.register('description')} />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={isBusy}>
                {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
                {editing ? 'Save changes' : 'Create department'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(deleting)} onOpenChange={(open) => !open && setDeleting(null)}>
        <DialogContent showCloseButton>
          <DialogHeader>
            <DialogTitle>Delete department</DialogTitle>
            <DialogDescription>
              Soft-delete <span className="font-medium">{deleting?.name}</span>? The code and name
              will become available again for new departments.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setDeleting(null)}>
              Cancel
            </Button>
            <Button type="button" variant="destructive" disabled={isBusy} onClick={onDelete}>
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
