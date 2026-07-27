'use client';

import { EyeIcon, PencilIcon, PlusIcon } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
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
import { AppointmentSubnav } from '@/features/appointments/components/shared/appointment-subnav';
import { useAppointmentLookups } from '@/features/appointments/hooks/use-appointment-lookups';
import { useAppointmentsQuery } from '@/features/appointments/hooks/use-appointments';
import {
  formatAppointmentSlot,
  isMutableAppointmentStatus,
} from '@/features/appointments/lib/appointment-format';
import {
  resetAppointmentsFilters,
  selectAppointmentsListUi,
  setAppointmentsAppointmentNumber,
  setAppointmentsDepartmentName,
  setAppointmentsDoctorName,
  setAppointmentsFromDate,
  setAppointmentsPage,
  setAppointmentsPatientName,
  setAppointmentsQueueStatus,
  setAppointmentsStatus,
  setAppointmentsToDate,
  setAppointmentsVisitType,
} from '@/features/appointments/store/appointments-ui-slice';
import {
  APPOINTMENT_STATUSES,
  QUEUE_ENTRY_STATUSES,
  VISIT_TYPES,
  type AppointmentStatus,
  type QueueEntryStatus,
  type VisitType,
} from '@/features/appointments/types/enums';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { FilterSelect } from '@/features/hospital-admin/components/shared/filter-select';
import { ListToolbar } from '@/features/hospital-admin/components/shared/list-toolbar';
import { PaginationControls } from '@/features/hospital-admin/components/shared/pagination-controls';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

const SEARCH_DEBOUNCE_MS = 300;

export function AppointmentsListPage() {
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectAppointmentsListUi);

  const [appointmentNumberInput, setAppointmentNumberInput] = useState(ui.appointmentNumber);
  const [patientNameInput, setPatientNameInput] = useState(ui.patientName);
  const [doctorNameInput, setDoctorNameInput] = useState(ui.doctorName);
  const [departmentNameInput, setDepartmentNameInput] = useState(ui.departmentName);

  useEffect(() => {
    setAppointmentNumberInput(ui.appointmentNumber);
    setPatientNameInput(ui.patientName);
    setDoctorNameInput(ui.doctorName);
    setDepartmentNameInput(ui.departmentName);
  }, [ui.appointmentNumber, ui.patientName, ui.doctorName, ui.departmentName]);

  useEffect(() => {
    const handle = window.setTimeout(() => {
      if (appointmentNumberInput !== ui.appointmentNumber) {
        dispatch(setAppointmentsAppointmentNumber(appointmentNumberInput));
      }
      if (patientNameInput !== ui.patientName) {
        dispatch(setAppointmentsPatientName(patientNameInput));
      }
      if (doctorNameInput !== ui.doctorName) {
        dispatch(setAppointmentsDoctorName(doctorNameInput));
      }
      if (departmentNameInput !== ui.departmentName) {
        dispatch(setAppointmentsDepartmentName(departmentNameInput));
      }
    }, SEARCH_DEBOUNCE_MS);
    return () => window.clearTimeout(handle);
  }, [
    appointmentNumberInput,
    patientNameInput,
    doctorNameInput,
    departmentNameInput,
    dispatch,
    ui.appointmentNumber,
    ui.patientName,
    ui.doctorName,
    ui.departmentName,
  ]);

  const query = useMemo(
    () => ({
      appointmentNumber: ui.appointmentNumber.trim() || undefined,
      patientName: ui.patientName.trim() || undefined,
      doctorName: ui.doctorName.trim() || undefined,
      departmentName: ui.departmentName.trim() || undefined,
      status: (ui.status || undefined) as AppointmentStatus | undefined,
      visitType: (ui.visitType || undefined) as VisitType | undefined,
      queueStatus: (ui.queueStatus || undefined) as QueueEntryStatus | undefined,
      fromDate: ui.fromDate || undefined,
      toDate: ui.toDate || undefined,
      page: ui.page,
      size: ui.size,
      sort: ['appointmentDate,desc', 'startTime,asc'],
    }),
    [ui],
  );

  const appointmentsQuery = useAppointmentsQuery(query);
  const rows = appointmentsQuery.data?.content ?? [];
  const lookups = useAppointmentLookups(rows);

  const hasFilters = Boolean(
    ui.appointmentNumber ||
      ui.patientName ||
      ui.doctorName ||
      ui.departmentName ||
      ui.status ||
      ui.visitType ||
      ui.queueStatus ||
      ui.fromDate ||
      ui.toDate,
  );

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="Appointment list"
        description="Search and filter appointments with server-side pagination. Filters never load the full directory into memory."
        actions={
          <Can permissions={[Permissions.APPOINTMENT_CREATE]}>
            <Button nativeButton={false} render={<Link href="/app/appointments/new" />}>
              <PlusIcon data-icon="inline-start" />
              Book appointment
            </Button>
          </Can>
        }
      />

      <AppointmentSubnav />

      <div className="rounded-xl border bg-card">
        <div className="space-y-3 border-b p-4">
          <ListToolbar
            search={patientNameInput}
            onSearchChange={setPatientNameInput}
            searchPlaceholder="Patient name…"
            showReset={hasFilters}
            onReset={() => {
              setAppointmentNumberInput('');
              setPatientNameInput('');
              setDoctorNameInput('');
              setDepartmentNameInput('');
              dispatch(resetAppointmentsFilters());
            }}
            filters={
              <>
                <FilterSelect
                  value={ui.status}
                  onValueChange={(status) => dispatch(setAppointmentsStatus(status))}
                  options={APPOINTMENT_STATUSES}
                  placeholder="Status"
                  allLabel="All statuses"
                />
                <FilterSelect
                  value={ui.visitType}
                  onValueChange={(visitType) => dispatch(setAppointmentsVisitType(visitType))}
                  options={VISIT_TYPES}
                  placeholder="Visit type"
                  allLabel="All visit types"
                />
                <FilterSelect
                  value={ui.queueStatus}
                  onValueChange={(queueStatus) =>
                    dispatch(setAppointmentsQueueStatus(queueStatus))
                  }
                  options={QUEUE_ENTRY_STATUSES}
                  placeholder="Queue status"
                  allLabel="Any queue status"
                />
              </>
            }
          />

          <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
            <Input
              value={appointmentNumberInput}
              onChange={(event) => setAppointmentNumberInput(event.target.value)}
              placeholder="Appointment number"
            />
            <Input
              value={doctorNameInput}
              onChange={(event) => setDoctorNameInput(event.target.value)}
              placeholder="Doctor name / code"
            />
            <Input
              value={departmentNameInput}
              onChange={(event) => setDepartmentNameInput(event.target.value)}
              placeholder="Department"
            />
            <div className="grid grid-cols-2 gap-2">
              <Input
                type="date"
                value={ui.fromDate}
                onChange={(event) => dispatch(setAppointmentsFromDate(event.target.value))}
                aria-label="From date"
              />
              <Input
                type="date"
                value={ui.toDate}
                onChange={(event) => dispatch(setAppointmentsToDate(event.target.value))}
                aria-label="To date"
              />
            </div>
          </div>
        </div>

        {appointmentsQuery.isError ? (
          <EmptyState
            title="Unable to load appointments"
            description={getErrorMessage(appointmentsQuery.error)}
          />
        ) : appointmentsQuery.isLoading ? (
          <div className="text-muted-foreground px-6 py-16 text-center text-sm">Loading…</div>
        ) : rows.length === 0 ? (
          <EmptyState
            title="No appointments found"
            description="Book an appointment or adjust search filters."
            action={
              <Can permissions={[Permissions.APPOINTMENT_CREATE]}>
                <Button
                  nativeButton={false}
                  variant="outline"
                  render={<Link href="/app/appointments/new" />}
                >
                  Book appointment
                </Button>
              </Can>
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Number</TableHead>
                  <TableHead>When</TableHead>
                  <TableHead>Patient</TableHead>
                  <TableHead>Doctor</TableHead>
                  <TableHead>Department</TableHead>
                  <TableHead>Visit</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((appointment) => (
                  <TableRow key={appointment.id}>
                    <TableCell className="font-mono text-xs">
                      {appointment.appointmentNumber}
                    </TableCell>
                    <TableCell className="whitespace-nowrap text-sm">
                      {formatAppointmentSlot(
                        appointment.appointmentDate,
                        appointment.startTime,
                        appointment.endTime,
                      )}
                    </TableCell>
                    <TableCell>
                      {appointment.patientName
                        ?? lookups.patientNameById.get(appointment.patientId)
                        ?? '—'}
                    </TableCell>
                    <TableCell>
                      {lookups.doctorNameById.get(appointment.doctorId) ?? '—'}
                    </TableCell>
                    <TableCell>
                      {lookups.departmentNameById.get(appointment.departmentId) ?? '—'}
                    </TableCell>
                    <TableCell>{formatEnumLabel(appointment.visitType)}</TableCell>
                    <TableCell>
                      <StatusBadge status={appointment.status} />
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-1">
                        <Button
                          nativeButton={false}
                          variant="ghost"
                          size="icon-sm"
                          render={<Link href={`/app/appointments/${appointment.id}`} />}
                          aria-label="View appointment"
                        >
                          <EyeIcon />
                        </Button>
                        {isMutableAppointmentStatus(appointment.status) ? (
                          <Can permissions={[Permissions.APPOINTMENT_UPDATE]}>
                            <Button
                              nativeButton={false}
                              variant="ghost"
                              size="icon-sm"
                              render={
                                <Link href={`/app/appointments/${appointment.id}/edit`} />
                              }
                              aria-label="Edit appointment"
                            >
                              <PencilIcon />
                            </Button>
                          </Can>
                        ) : null}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}

        {appointmentsQuery.data ? (
          <div className="border-t p-4">
            <PaginationControls
              page={appointmentsQuery.data.page}
              totalPages={appointmentsQuery.data.totalPages}
              totalElements={appointmentsQuery.data.totalElements}
              size={appointmentsQuery.data.size}
              onPageChange={(page) => dispatch(setAppointmentsPage(page))}
            />
          </div>
        ) : null}
      </div>
    </div>
  );
}
