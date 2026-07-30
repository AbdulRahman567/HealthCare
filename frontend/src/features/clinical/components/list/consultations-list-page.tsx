'use client';

import { EyeIcon, PlusIcon } from 'lucide-react';
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
import { ClinicalSubnav } from '@/features/clinical/components/shared/clinical-subnav';
import { useConsultationsQuery } from '@/features/clinical/hooks/use-consultations';
import {
  resetClinicalFilters,
  selectClinicalListUi,
  setClinicalConsultationNumber,
  setClinicalDoctorName,
  setClinicalFromDate,
  setClinicalPage,
  setClinicalPatientName,
  setClinicalStatus,
  setClinicalToDate,
} from '@/features/clinical/store/clinical-ui-slice';
import { CONSULTATION_STATUSES, type ConsultationStatus } from '@/features/clinical/types/enums';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { FilterSelect } from '@/features/hospital-admin/components/shared/filter-select';
import { ListToolbar } from '@/features/hospital-admin/components/shared/list-toolbar';
import { PaginationControls } from '@/features/hospital-admin/components/shared/pagination-controls';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

const SEARCH_DEBOUNCE_MS = 300;

export function ConsultationsListPage() {
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectClinicalListUi);

  const [consultationNumberInput, setConsultationNumberInput] = useState(ui.consultationNumber);
  const [patientNameInput, setPatientNameInput] = useState(ui.patientName);
  const [doctorNameInput, setDoctorNameInput] = useState(ui.doctorName);

  useEffect(() => {
    setConsultationNumberInput(ui.consultationNumber);
    setPatientNameInput(ui.patientName);
    setDoctorNameInput(ui.doctorName);
  }, [ui.consultationNumber, ui.patientName, ui.doctorName]);

  useEffect(() => {
    const handle = window.setTimeout(() => {
      if (consultationNumberInput !== ui.consultationNumber) {
        dispatch(setClinicalConsultationNumber(consultationNumberInput));
      }
      if (patientNameInput !== ui.patientName) {
        dispatch(setClinicalPatientName(patientNameInput));
      }
      if (doctorNameInput !== ui.doctorName) {
        dispatch(setClinicalDoctorName(doctorNameInput));
      }
    }, SEARCH_DEBOUNCE_MS);
    return () => window.clearTimeout(handle);
  }, [
    consultationNumberInput,
    patientNameInput,
    doctorNameInput,
    dispatch,
    ui.consultationNumber,
    ui.patientName,
    ui.doctorName,
  ]);

  const query = useMemo(
    () => ({
      consultationNumber: ui.consultationNumber.trim() || undefined,
      patientName: ui.patientName.trim() || undefined,
      doctorName: ui.doctorName.trim() || undefined,
      status: (ui.status || undefined) as ConsultationStatus | undefined,
      fromDate: ui.fromDate || undefined,
      toDate: ui.toDate || undefined,
      page: ui.page,
      size: ui.size,
      sort: ['consultationDate,desc'],
    }),
    [ui],
  );

  const listQuery = useConsultationsQuery(query);
  const rows = listQuery.data?.content ?? [];

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      <AdminPageHeader
        title="Clinical consultations"
        description="Doctor encounter workspace — chart, vitals, diagnoses, prescriptions, notes, and follow-ups."
        actions={
          <Can permissions={[Permissions.VISIT_CREATE]}>
            <Button nativeButton={false} render={<Link href="/app/clinical/new" />}>
              <PlusIcon data-icon="inline-start" />
              Start consultation
            </Button>
          </Can>
        }
      />

      <ClinicalSubnav />

      <ListToolbar
        search={patientNameInput}
        onSearchChange={setPatientNameInput}
        searchPlaceholder="Search patient name…"
        showReset
        onReset={() => dispatch(resetClinicalFilters())}
        filters={
          <>
            <Input
              value={consultationNumberInput}
              onChange={(event) => setConsultationNumberInput(event.target.value)}
              placeholder="Consultation #"
              aria-label="Filter by consultation number"
              className="w-full sm:w-40"
            />
            <Input
              value={doctorNameInput}
              onChange={(event) => setDoctorNameInput(event.target.value)}
              placeholder="Doctor"
              aria-label="Filter by doctor name"
              className="w-full sm:w-40"
            />
            <FilterSelect
              value={ui.status}
              onValueChange={(value) => dispatch(setClinicalStatus(value))}
              options={CONSULTATION_STATUSES}
              placeholder="Status"
              allLabel="All statuses"
            />
            <Input
              type="date"
              value={ui.fromDate}
              onChange={(event) => dispatch(setClinicalFromDate(event.target.value))}
              aria-label="From date"
              className="w-full sm:w-40"
            />
            <Input
              type="date"
              value={ui.toDate}
              onChange={(event) => dispatch(setClinicalToDate(event.target.value))}
              aria-label="To date"
              className="w-full sm:w-40"
            />
          </>
        }
      />

      {listQuery.isError ? (
        <EmptyState
          title="Unable to load consultations"
          description={getErrorMessage(listQuery.error)}
        />
      ) : listQuery.isLoading ? (
        <div className="text-muted-foreground py-16 text-center text-sm">
          Loading consultations…
        </div>
      ) : rows.length === 0 ? (
        <EmptyState
          title="No consultations found"
          description="Start a consultation from an appointment check-in or create one here."
          action={
            <Can permissions={[Permissions.VISIT_CREATE]}>
              <Button nativeButton={false} render={<Link href="/app/clinical/new" />}>
                Start consultation
              </Button>
            </Can>
          }
        />
      ) : (
        <div className="overflow-x-auto rounded-xl border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Consultation</TableHead>
                <TableHead>Patient</TableHead>
                <TableHead>Doctor</TableHead>
                <TableHead>Department</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.id}>
                  <TableCell className="font-medium">{row.consultationNumber}</TableCell>
                  <TableCell>
                    <div className="flex flex-col">
                      <span>{row.patientName || '—'}</span>
                      <span className="text-muted-foreground text-xs">{row.patientMrn}</span>
                    </div>
                  </TableCell>
                  <TableCell>{row.doctorName || '—'}</TableCell>
                  <TableCell>{row.departmentName || '—'}</TableCell>
                  <TableCell>{row.consultationDate}</TableCell>
                  <TableCell>
                    <StatusBadge status={row.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <Can permissions={[Permissions.VISIT_READ]}>
                      <Button
                        size="sm"
                        variant="outline"
                        nativeButton={false}
                        render={<Link href={`/app/clinical/${row.id}`} />}
                      >
                        <EyeIcon data-icon="inline-start" />
                        Open
                      </Button>
                    </Can>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {listQuery.data ? (
        <PaginationControls
          page={listQuery.data.page}
          totalPages={listQuery.data.totalPages}
          totalElements={listQuery.data.totalElements}
          size={listQuery.data.size}
          onPageChange={(page) => dispatch(setClinicalPage(page))}
        />
      ) : null}
    </div>
  );
}
