'use client';

import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { formatEnumLabel } from '@/lib/page-query';
import { cn } from '@/lib/utils';

type EnumSelectProps = {
  id?: string;
  label: string;
  value: string;
  onValueChange: (value: string) => void;
  options: readonly string[];
  labels?: Record<string, string>;
  placeholder?: string;
  error?: string;
  optional?: boolean;
  emptyLabel?: string;
  className?: string;
  disabled?: boolean;
};

export function EnumSelect({
  id,
  label,
  value,
  onValueChange,
  options,
  labels,
  placeholder,
  error,
  optional,
  emptyLabel = 'Not specified',
  className,
  disabled,
}: EnumSelectProps) {
  const selectValue = value || (optional ? '__empty__' : '');

  return (
    <div className={cn('space-y-2', className)}>
      <Label htmlFor={id}>{label}</Label>
      <Select
        value={selectValue}
        onValueChange={(next) => onValueChange(next === '__empty__' ? '' : (next ?? ''))}
        disabled={disabled}
      >
        <SelectTrigger id={id} className="w-full" aria-invalid={Boolean(error)}>
          <SelectValue placeholder={placeholder ?? label} />
        </SelectTrigger>
        <SelectContent>
          {optional ? <SelectItem value="__empty__">{emptyLabel}</SelectItem> : null}
          {options.map((option) => (
            <SelectItem key={option} value={option}>
              {labels?.[option] ?? formatEnumLabel(option)}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      {error ? <p className="text-destructive text-xs">{error}</p> : null}
    </div>
  );
}

type FieldErrorProps = {
  message?: string;
};

export function FieldError({ message }: FieldErrorProps) {
  if (!message) {
    return null;
  }
  return <p className="text-destructive text-xs">{message}</p>;
}
