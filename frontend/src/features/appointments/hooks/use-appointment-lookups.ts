'use client';

import { useMemo } from 'react';
import { useQueries } from '@tanstack/react-query';

import { useDepartmentsQuery } from '@/features/hospital-admin/hooks/use-departments';
import { useStaffQuery } from '@/features/hospital-admin/hooks/use-staff';
import { useUsersQuery } from '@/features/hospital-admin/hooks/use-users';
import { patientsApi } from '@/features/patients/api/patients-api';
import { patientDisplayName } from '@/features/patients/lib/patient-format';
import { formatPersonName } from '@/lib/page-query';
import type { AppointmentResponse } from '@/features/appointments/types/appointment';

/**
 * Resolves display labels for appointment rows.
 * Prefers server-enriched patientName/patientMrn (batch on API) and only falls
 * back to detail GETs for ids still missing labels — never unbounded fan-out.
 */
export function useAppointmentLookups(appointments: AppointmentResponse[]) {
  const missingPatientIds = useMemo(() => {
    const ids = new Set<string>();
    for (const row of appointments) {
      if (row.patientId && !row.patientName) {
        ids.add(row.patientId);
      }
    }
    return [...ids];
  }, [appointments]);

  const patientQueries = useQueries({
    queries: missingPatientIds.map((id) => ({
      queryKey: ['patients', 'detail', id],
      queryFn: () => patientsApi.getById(id),
      enabled: Boolean(id),
      staleTime: 60_000,
    })),
  });

  const doctorsQuery = useStaffQuery('DOCTOR', {
    page: 0,
    size: 100,
    sort: 'employeeCode,asc',
    employmentStatus: 'ACTIVE',
  });

  const usersQuery = useUsersQuery({
    page: 0,
    size: 100,
    status: 'ACTIVE',
    roleType: 'DOCTOR',
    sort: ['lastName,asc', 'firstName,asc'],
  });

  const departmentsQuery = useDepartmentsQuery({
    page: 0,
    size: 100,
    sort: 'name,asc',
    status: 'ACTIVE',
  });

  const patientNameById = useMemo(() => {
    const map = new Map<string, string>();
    for (const row of appointments) {
      if (row.patientId && row.patientName) {
        map.set(row.patientId, row.patientName);
      }
    }
    for (const query of patientQueries) {
      if (query.data) {
        map.set(query.data.id, patientDisplayName(query.data));
      }
    }
    return map;
  }, [appointments, patientQueries]);

  const doctorNameById = useMemo(() => {
    const userNameById = new Map<string, string>();
    for (const user of usersQuery.data?.content ?? []) {
      userNameById.set(user.id, formatPersonName(user.firstName, user.lastName));
    }
    const map = new Map<string, string>();
    for (const doctor of doctorsQuery.data?.content ?? []) {
      const name = userNameById.get(doctor.userId);
      map.set(doctor.id, name ? `${name} (${doctor.employeeCode})` : doctor.employeeCode);
    }
    return map;
  }, [doctorsQuery.data, usersQuery.data]);

  const departmentNameById = useMemo(() => {
    const map = new Map<string, string>();
    for (const department of departmentsQuery.data?.content ?? []) {
      map.set(department.id, department.name);
    }
    return map;
  }, [departmentsQuery.data]);

  const doctors = doctorsQuery.data?.content ?? [];
  const departments = departmentsQuery.data?.content ?? [];
  const hospitalId = doctors[0]?.hospitalId ?? departments[0]?.hospitalId ?? '';

  return {
    patientNameById,
    doctorNameById,
    departmentNameById,
    doctors,
    departments,
    hospitalId,
    doctorsQuery,
    departmentsQuery,
    usersQuery,
  };
}
