'use client';

import { SearchIcon, XIcon } from 'lucide-react';
import type { ReactNode } from 'react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

type ListToolbarProps = {
  search: string;
  onSearchChange: (value: string) => void;
  searchPlaceholder?: string;
  filters?: ReactNode;
  onReset?: () => void;
  showReset?: boolean;
};

export function ListToolbar({
  search,
  onSearchChange,
  searchPlaceholder = 'Search…',
  filters,
  onReset,
  showReset,
}: ListToolbarProps) {
  return (
    <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
      <div className="relative w-full max-w-md">
        <SearchIcon className="text-muted-foreground pointer-events-none absolute top-1/2 left-2.5 size-4 -translate-y-1/2" />
        <Input
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder={searchPlaceholder}
          className="pl-8"
          aria-label="Search"
        />
      </div>
      <div className="flex flex-wrap items-center gap-2">
        {filters}
        {showReset && onReset ? (
          <Button type="button" variant="ghost" size="sm" onClick={onReset}>
            <XIcon data-icon="inline-start" />
            Reset
          </Button>
        ) : null}
      </div>
    </div>
  );
}
