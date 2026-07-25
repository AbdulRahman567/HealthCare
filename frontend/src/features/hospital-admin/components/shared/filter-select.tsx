'use client';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { formatEnumLabel } from '@/lib/page-query';
import { cn } from '@/lib/utils';

type FilterSelectProps = {
  value: string;
  onValueChange: (value: string) => void;
  options: readonly string[];
  placeholder: string;
  allLabel?: string;
  className?: string;
  labels?: Record<string, string>;
};

export function FilterSelect({
  value,
  onValueChange,
  options,
  placeholder,
  allLabel = 'All',
  className,
  labels,
}: FilterSelectProps) {
  return (
    <Select
      value={value || '__all__'}
      onValueChange={(next) => onValueChange(next === '__all__' ? '' : (next ?? ''))}
    >
      <SelectTrigger className={cn('w-full min-w-40 sm:w-auto', className)}>
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="__all__">{allLabel}</SelectItem>
        {options.map((option) => (
          <SelectItem key={option} value={option}>
            {labels?.[option] ?? formatEnumLabel(option)}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
