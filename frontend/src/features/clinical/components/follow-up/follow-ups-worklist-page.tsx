'use client';

import Link from 'next/link';
import { useMemo } from 'react';

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
import {
  useFollowUpsDueQuery,
  useFollowUpsSearchQuery,
} from '@/features/clinical/hooks/use-follow-ups';
import {
  resetFollowUpFilters,
  selectFollowUpWorklistUi,
  setFollowUpOverdueOnly,
  setFollowUpPage,
  setFollowUpPriorityFilter,
  setFollowUpStatusFilter,
  setFollowUpWithinDays,
} from '@/features/clinical/store/clinical-ui-slice';
import {
  FOLLOW_UP_PRIORITIES,
  FOLLOW_UP_STATUSES,
  type FollowUpPriority,
  type FollowUpStatus,
} from '@/features/clinical/types/enums';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { FilterSelect } from '@/features/hospital-admin/components/shared/filter-select';
import { ListToolbar } from '@/features/hospital-admin/components/shared/list-toolbar';
import { PaginationControls } from '@/features/hospital-admin/components/shared/pagination-controls';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

export function FollowUpsWorklistPage() {
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectFollowUpWorklistUi);

  const dueQuery = useFollowUpsDueQuery(ui.withinDays);

  const searchQuery = useMemo(
    () => ({
      status: (ui.status || undefined) as FollowUpStatus | undefined,
      priority: (ui.priority || undefined) as FollowUpPriority | undefined,
      overdueOnly: ui.overdueOnly || undefined,
      dueWithinDays: ui.withinDays,
      page: ui.page,
      size: ui.size,
      sort: ['scheduledDate,asc'],
    }),
    [ui],
  );

  const listQuery = useFollowUpsSearchQuery(searchQuery);
  const rows = listQuery.data?.content ?? [];
  const dueRows = dueQuery.data ?? [];

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      <AdminPageHeader
        title="Follow-up worklist"
        description="Due and overdue return visits for clinical follow-through."
        actions={
          <Can permissions={[Permissions.VISIT_READ]}>
            <Button nativeButton={false} variant="outline" render={<Link href="/app/clinical" />}>
              Consultations
            </Button>
          </Can>
        }
      />

      <ClinicalSubnav />

      <section className="rounded-xl border bg-card p-4">
        <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
          <h2 className="text-sm font-semibold">Doctor due list (next {ui.withinDays} days)</h2>
          <Input
            type="number"
            min={1}
            max={90}
            className="w-28"
            value={ui.withinDays}
            onChange={(event) =>
              dispatch(setFollowUpWithinDays(Math.min(90, Math.max(1, Number(event.target.value) || 14))))
            }
            aria-label="Due within days"
          />
        </div>
        {dueQuery.isError ? (
          <EmptyState
            title="Unable to load due follow-ups"
            description={getErrorMessage(dueQuery.error)}
          />
        ) : dueQuery.isLoading ? (
          <div className="text-muted-foreground py-6 text-center text-sm">Loading due list…</div>
        ) : dueRows.length === 0 ? (
          <EmptyState
            title="No due follow-ups"
            description="Nothing scheduled in the selected window."
          />
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Date</TableHead>
                  <TableHead>Consultation</TableHead>
                  <TableHead>Priority</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Reason</TableHead>
                  <TableHead className="text-right">Open</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dueRows.map((row) => (
                  <TableRow key={row.id}>
                    <TableCell>
                      {row.scheduledDate}
                      {row.scheduledTime ? ` · ${row.scheduledTime}` : ''}
                    </TableCell>
                    <TableCell>{row.consultationNumber}</TableCell>
                    <TableCell>
                      <StatusBadge status={row.priority} />
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={row.status} />
                    </TableCell>
                    <TableCell className="max-w-xs truncate">{row.reason || '—'}</TableCell>
                    <TableCell className="text-right">
                      <Button
                        size="sm"
                        variant="outline"
                        nativeButton={false}
                        render={<Link href={`/app/clinical/${row.consultationId}`} />}
                      >
                        Open
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </section>

      <ListToolbar
        search=""
        onSearchChange={() => undefined}
        searchPlaceholder="Use filters below"
        showReset
        onReset={() => dispatch(resetFollowUpFilters())}
        filters={
          <>
            <FilterSelect
              value={ui.status}
              onValueChange={(value) => dispatch(setFollowUpStatusFilter(value))}
              options={FOLLOW_UP_STATUSES}
              placeholder="Status"
              allLabel="All statuses"
            />
            <FilterSelect
              value={ui.priority}
              onValueChange={(value) => dispatch(setFollowUpPriorityFilter(value))}
              options={FOLLOW_UP_PRIORITIES}
              placeholder="Priority"
              allLabel="All priorities"
            />
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={ui.overdueOnly}
                onChange={(event) => dispatch(setFollowUpOverdueOnly(event.target.checked))}
              />
              Overdue only
            </label>
          </>
        }
      />

      {listQuery.isError ? (
        <EmptyState
          title="Unable to search follow-ups"
          description={getErrorMessage(listQuery.error)}
        />
      ) : listQuery.isLoading ? (
        <div className="text-muted-foreground py-10 text-center text-sm">Searching…</div>
      ) : rows.length === 0 ? (
        <EmptyState title="No follow-ups match" description="Adjust filters or widen the date window." />
      ) : (
        <div className="overflow-x-auto rounded-xl border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Date</TableHead>
                <TableHead>Consultation</TableHead>
                <TableHead>Doctor</TableHead>
                <TableHead>Priority</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Open</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.id}>
                  <TableCell>{row.scheduledDate}</TableCell>
                  <TableCell>{row.consultationNumber}</TableCell>
                  <TableCell>{row.doctorName || '—'}</TableCell>
                  <TableCell>
                    <StatusBadge status={row.priority} />
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={row.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      size="sm"
                      variant="outline"
                      nativeButton={false}
                      render={<Link href={`/app/clinical/${row.consultationId}`} />}
                    >
                      Open
                    </Button>
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
          onPageChange={(page) => dispatch(setFollowUpPage(page))}
        />
      ) : null}
    </div>
  );
}
