'use client';

import { usePathname } from 'next/navigation';
import { useMemo } from 'react';

import { buildBreadcrumbs } from '@/features/navigation/lib/navigation';

export function useBreadcrumbs() {
  const pathname = usePathname();
  return useMemo(() => buildBreadcrumbs(pathname), [pathname]);
}
