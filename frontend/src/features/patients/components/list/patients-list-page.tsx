'use client';

import { EyeIcon, PlusIcon } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';

import { Button } from '@/components/ui/button';
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
import { usePatientsQuery } from '@/features/patients/hooks/use-patients';
import {
  calculateAge,
  formatBloodGroup,
  formatDate,
  patientDisplayName,
} from '@/features/patients/lib/patient-format';
import {
  resetPatientsFilters,
  selectPatientsListUi,
  setPatientsBloodGroup,
  setPatientsGender,
  setPatientsPage,
  setPatientsSearch,
  setPatientsStatus,
} from '@/features/patients/store/patients-ui-slice';
import {
  BLOOD_GROUPS,
  BLOOD_GROUP_LABELS,
  GENDERS,
  PATIENT_STATUSES,
  type BloodGroup,
  type Gender,
  type PatientStatus,
} from '@/features/patients/types/enums';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

const SEARCH_DEBOUNCE_MS = 300;

export function PatientsListPage() {
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectPatientsListUi);
  const [searchInput, setSearchInput] = useState(ui.q);

  useEffect(() => {
    setSearchInput(ui.q);
  }, [ui.q]);

  useEffect(() => {
    const handle = window.setTimeout(() => {
      if (searchInput !== ui.q) {
        dispatch(setPatientsSearch(searchInput));
      }
    }, SEARCH_DEBOUNCE_MS);
    return () => window.clearTimeout(handle);
  }, [dispatch, searchInput, ui.q]);

  const query = useMemo(
    () => ({
      q: ui.q.trim() || undefined,
      status: (ui.status || undefined) as PatientStatus | undefined,
      gender: (ui.gender || undefined) as Gender | undefined,
      bloodGroup: (ui.bloodGroup || undefined) as BloodGroup | undefined,
      page: ui.page,
      size: ui.size,
      sort: ['lastName,asc', 'firstName,asc'],
    }),
    [ui],
  );

  const patientsQuery = usePatientsQuery(query);
  const rows = patientsQuery.data?.content ?? [];
  const hasFilters = Boolean(ui.q || ui.status || ui.gender || ui.bloodGroup);

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="Patients"
        description="Search and open patient charts. Filters run against the hospital directory with server-side pagination."
        actions={
          <Can permissions={[Permissions.PATIENT_CREATE]}>
            <Button nativeButton={false} render={<Link href="/app/patients/new" />}>
              <PlusIcon data-icon="inline-start" />
              Register patient
            </Button>
          </Can>
        }
      />

      <div className="rounded-xl border bg-card">
        <div className="border-b p-4">
          <ListToolbar
            search={searchInput}
            onSearchChange={setSearchInput}
            searchPlaceholder="Search MRN, name, phone, email, CNIC…"
            showReset={hasFilters || Boolean(searchInput)}
            onReset={() => {
              setSearchInput('');
              dispatch(resetPatientsFilters());
            }}
            filters={
              <>
                <FilterSelect
                  value={ui.status}
                  onValueChange={(status) => dispatch(setPatientsStatus(status))}
                  options={PATIENT_STATUSES}
                  placeholder="Status"
                  allLabel="All statuses"
                />
                <FilterSelect
                  value={ui.gender}
                  onValueChange={(gender) => dispatch(setPatientsGender(gender))}
                  options={GENDERS}
                  placeholder="Gender"
                  allLabel="All genders"
                />
                <FilterSelect
                  value={ui.bloodGroup}
                  onValueChange={(bloodGroup) => dispatch(setPatientsBloodGroup(bloodGroup))}
                  options={BLOOD_GROUPS}
                  labels={BLOOD_GROUP_LABELS}
                  placeholder="Blood group"
                  allLabel="All blood groups"
                />
              </>
            }
          />
        </div>

        {patientsQuery.isError ? (
          <EmptyState
            title="Unable to load patients"
            description={getErrorMessage(patientsQuery.error)}
          />
        ) : patientsQuery.isLoading ? (
          <div className="text-muted-foreground px-6 py-16 text-center text-sm">Loading…</div>
        ) : rows.length === 0 ? (
          <EmptyState
            title="No patients found"
            description="Register a patient or adjust search filters."
            action={
              <Can permissions={[Permissions.PATIENT_CREATE]}>
                <Button
                  nativeButton={false}
                  variant="outline"
                  render={<Link href="/app/patients/new" />}
                >
                  Register patient
                </Button>
              </Can>
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>MRN</TableHead>
                  <TableHead>Name</TableHead>
                  <TableHead>DOB / Age</TableHead>
                  <TableHead>Gender</TableHead>
                  <TableHead>Blood</TableHead>
                  <TableHead>Phone</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((patient) => {
                  const age = calculateAge(patient.dateOfBirth);
                  return (
                    <TableRow key={patient.id}>
                      <TableCell className="font-mono text-xs">{patient.mrn}</TableCell>
                      <TableCell className="font-medium">{patientDisplayName(patient)}</TableCell>
                      <TableCell>
                        <div className="flex flex-col">
                          <span>{formatDate(patient.dateOfBirth)}</span>
                          <span className="text-muted-foreground text-xs">
                            {age !== null ? `${age} yrs` : '—'}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>{formatEnumLabel(patient.gender)}</TableCell>
                      <TableCell>{formatBloodGroup(patient.bloodGroup)}</TableCell>
                      <TableCell>{patient.phone || '—'}</TableCell>
                      <TableCell>
                        <StatusBadge status={patient.status} />
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          nativeButton={false}
                          variant="ghost"
                          size="icon-sm"
                          render={<Link href={`/app/patients/${patient.id}`} />}
                          aria-label={`Open chart for ${patientDisplayName(patient)}`}
                        >
                          <EyeIcon />
                        </Button>
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
          totalPages={patientsQuery.data?.totalPages ?? 0}
          totalElements={patientsQuery.data?.totalElements ?? 0}
          onPageChange={(page) => dispatch(setPatientsPage(page))}
          disabled={patientsQuery.isFetching}
        />
      </div>
    </div>
  );
}
