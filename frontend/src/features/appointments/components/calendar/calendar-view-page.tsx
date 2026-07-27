'use client';

import Link from 'next/link';
import { useEffect, useMemo } from 'react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { AppointmentSubnav } from '@/features/appointments/components/shared/appointment-subnav';
import {
  toCalendarScopeKind,
  useCalendarDailyQuery,
  useCalendarMonthlyQuery,
  useCalendarWeeklyQuery,
} from '@/features/appointments/hooks/use-calendar';
import { useAppointmentLookups } from '@/features/appointments/hooks/use-appointment-lookups';
import { formatTimeLabel } from '@/features/appointments/lib/appointment-format';
import {
  selectAppointmentsCalendarUi,
  setCalendarDate,
  setCalendarScope,
  setCalendarScopeId,
  setCalendarStatus,
  setCalendarView,
  setCalendarYearMonth,
} from '@/features/appointments/store/appointments-ui-slice';
import {
  APPOINTMENT_STATUSES,
  CALENDAR_SCOPES,
  CALENDAR_VIEW_TYPES,
  type AppointmentStatus,
  type CalendarScope,
  type CalendarViewType,
} from '@/features/appointments/types/enums';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { FilterSelect } from '@/features/hospital-admin/components/shared/filter-select';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel, formatPersonName } from '@/lib/page-query';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { useUsersQuery } from '@/features/hospital-admin/hooks/use-users';

export function CalendarViewPage() {
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectAppointmentsCalendarUi);
  const lookups = useAppointmentLookups([]);
  const usersQuery = useUsersQuery({
    page: 0,
    size: 100,
    status: 'ACTIVE',
    roleType: 'DOCTOR',
    sort: ['lastName,asc', 'firstName,asc'],
  });

  const doctorOptions = useMemo(() => {
    const userNameById = new Map<string, string>();
    for (const user of usersQuery.data?.content ?? []) {
      userNameById.set(user.id, formatPersonName(user.firstName, user.lastName));
    }
    return lookups.doctors.map((doctor) => ({
      id: doctor.id,
      label: userNameById.get(doctor.userId)
        ? `${userNameById.get(doctor.userId)} (${doctor.employeeCode})`
        : doctor.employeeCode,
    }));
  }, [lookups.doctors, usersQuery.data]);

  const scopeOptions = useMemo(() => {
    if (ui.scope === 'DOCTOR') {
      return doctorOptions;
    }
    if (ui.scope === 'DEPARTMENT') {
      return lookups.departments.map((department) => ({
        id: department.id,
        label: department.name,
      }));
    }
    const hospitalId = lookups.hospitalId;
    return hospitalId ? [{ id: hospitalId, label: 'Current hospital' }] : [];
  }, [ui.scope, doctorOptions, lookups.departments, lookups.hospitalId]);

  useEffect(() => {
    if (!ui.scopeId && scopeOptions.length > 0) {
      dispatch(setCalendarScopeId(scopeOptions[0].id));
    }
  }, [dispatch, scopeOptions, ui.scopeId]);

  const scopeKind = toCalendarScopeKind(ui.scope);
  const status = (ui.status || undefined) as AppointmentStatus | undefined;

  const dailyQuery = useCalendarDailyQuery(
    scopeKind,
    ui.scopeId,
    { date: ui.date, status, page: 0, size: 50 },
    ui.view === 'DAILY' && Boolean(ui.scopeId),
  );
  const weeklyQuery = useCalendarWeeklyQuery(
    scopeKind,
    ui.scopeId,
    { date: ui.date, status, page: 0, size: 100 },
    ui.view === 'WEEKLY' && Boolean(ui.scopeId),
  );
  const monthlyQuery = useCalendarMonthlyQuery(
    scopeKind,
    ui.scopeId,
    { year: ui.year, month: ui.month, status },
    ui.view === 'MONTHLY' && Boolean(ui.scopeId),
  );

  const rangeData = ui.view === 'WEEKLY' ? weeklyQuery.data : dailyQuery.data;
  const rangeQuery = ui.view === 'WEEKLY' ? weeklyQuery : dailyQuery;
  const events = rangeData?.events.content ?? [];

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="Calendar"
        description="Daily, weekly, and monthly appointment calendars by doctor, department, or hospital."
      />

      <AppointmentSubnav />

      <div className="rounded-xl border bg-card p-4">
        <div className="grid gap-3 lg:grid-cols-4">
          <div className="space-y-2">
            <Label>Scope</Label>
            <Select
              value={ui.scope}
              onValueChange={(value) => dispatch(setCalendarScope(value as CalendarScope))}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {CALENDAR_SCOPES.map((scope) => (
                  <SelectItem key={scope} value={scope}>
                    {formatEnumLabel(scope)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2 lg:col-span-2">
            <Label>
              {ui.scope === 'DOCTOR'
                ? 'Doctor'
                : ui.scope === 'DEPARTMENT'
                  ? 'Department'
                  : 'Hospital'}
            </Label>
            <Select
              value={ui.scopeId || undefined}
              onValueChange={(value) => dispatch(setCalendarScopeId(value ?? ''))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select scope" />
              </SelectTrigger>
              <SelectContent>
                {scopeOptions.map((option) => (
                  <SelectItem key={option.id} value={option.id}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>View</Label>
            <Select
              value={ui.view}
              onValueChange={(value) => dispatch(setCalendarView(value as CalendarViewType))}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {CALENDAR_VIEW_TYPES.map((view) => (
                  <SelectItem key={view} value={view}>
                    {formatEnumLabel(view)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {ui.view === 'MONTHLY' ? (
            <div className="grid grid-cols-2 gap-2 lg:col-span-2">
              <div className="space-y-2">
                <Label htmlFor="cal-year">Year</Label>
                <Input
                  id="cal-year"
                  type="number"
                  value={ui.year}
                  onChange={(event) =>
                    dispatch(
                      setCalendarYearMonth({
                        year: Number(event.target.value) || ui.year,
                        month: ui.month,
                      }),
                    )
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="cal-month">Month</Label>
                <Input
                  id="cal-month"
                  type="number"
                  min={1}
                  max={12}
                  value={ui.month}
                  onChange={(event) =>
                    dispatch(
                      setCalendarYearMonth({
                        year: ui.year,
                        month: Number(event.target.value) || ui.month,
                      }),
                    )
                  }
                />
              </div>
            </div>
          ) : (
            <div className="space-y-2 lg:col-span-2">
              <Label htmlFor="cal-date">Date</Label>
              <Input
                id="cal-date"
                type="date"
                value={ui.date}
                onChange={(event) => dispatch(setCalendarDate(event.target.value))}
              />
            </div>
          )}

          <div className="space-y-2">
            <Label>Status filter</Label>
            <FilterSelect
              value={ui.status}
              onValueChange={(status) => dispatch(setCalendarStatus(status))}
              options={APPOINTMENT_STATUSES}
              placeholder="Status"
              allLabel="All statuses"
            />
          </div>
        </div>
      </div>

      {!ui.scopeId ? (
        <EmptyState
          title="Select a calendar scope"
          description="Choose a doctor, department, or hospital to load the schedule."
        />
      ) : ui.view === 'MONTHLY' ? (
        monthlyQuery.isError ? (
          <EmptyState
            title="Unable to load month"
            description={getErrorMessage(monthlyQuery.error)}
          />
        ) : monthlyQuery.isLoading || !monthlyQuery.data ? (
          <div className="text-muted-foreground py-16 text-center text-sm">Loading month…</div>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">
                {monthlyQuery.data.year}-{String(monthlyQuery.data.month).padStart(2, '0')}
              </CardTitle>
              <CardDescription>
                {monthlyQuery.data.totalAppointments} appointments this month
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
                {monthlyQuery.data.days.map((day) => (
                  <button
                    key={day.date}
                    type="button"
                    className="hover:bg-muted/50 rounded-lg border p-3 text-left transition-colors"
                    onClick={() => {
                      dispatch(setCalendarDate(day.date));
                      dispatch(setCalendarView('DAILY'));
                    }}
                  >
                    <p className="font-medium">{day.date}</p>
                    <p className="text-muted-foreground text-xs">{day.totalCount} appointments</p>
                  </button>
                ))}
              </div>
            </CardContent>
          </Card>
        )
      ) : rangeQuery.isError ? (
        <EmptyState
          title="Unable to load calendar"
          description={getErrorMessage(rangeQuery.error)}
        />
      ) : rangeQuery.isLoading || !rangeData ? (
        <div className="text-muted-foreground py-16 text-center text-sm">Loading calendar…</div>
      ) : events.length === 0 ? (
        <EmptyState
          title="No appointments in range"
          description={`${rangeData.fromDate} → ${rangeData.toDate}`}
        />
      ) : (
        <div className="space-y-4">
          <p className="text-muted-foreground text-sm">
            {rangeData.fromDate}
            {rangeData.fromDate !== rangeData.toDate ? ` → ${rangeData.toDate}` : ''}
            {' · '}
            {rangeData.events.totalElements} events
          </p>
          <div className="grid gap-3">
            {events.map((event) => (
              <Card key={event.id}>
                <CardContent className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between">
                  <div className="space-y-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="font-medium">
                        {formatTimeLabel(event.startTime)}–{formatTimeLabel(event.endTime)}
                      </p>
                      <StatusBadge status={event.status} />
                    </div>
                    <p className="text-sm">
                      {event.patientName ?? 'Patient'}
                      <span className="text-muted-foreground">
                        {' '}
                        · {event.appointmentNumber}
                        {event.patientMrn ? ` · ${event.patientMrn}` : ''}
                      </span>
                    </p>
                    <p className="text-muted-foreground text-xs">
                      {event.doctorName ?? 'Doctor'}
                      {event.departmentName ? ` · ${event.departmentName}` : ''}
                      {' · '}
                      {formatEnumLabel(event.visitType)}
                    </p>
                  </div>
                  <Button
                    nativeButton={false}
                    variant="outline"
                    size="sm"
                    render={<Link href={`/app/appointments/${event.id}`} />}
                  >
                    Open
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
