import {
  Building2,
  CalendarDays,
  CalendarPlus,
  FilePlus,
  HeartPulse,
  LayoutDashboard,
  Receipt,
  Settings2,
  UserPlus,
  UserRound,
  Users,
  type LucideIcon,
} from 'lucide-react';

import type { NavIconName } from '@/features/navigation/types';

const NAV_ICONS: Record<NavIconName, LucideIcon> = {
  'layout-dashboard': LayoutDashboard,
  'user-round': UserRound,
  'building-2': Building2,
  users: Users,
  'heart-pulse': HeartPulse,
  'calendar-days': CalendarDays,
  receipt: Receipt,
  'user-plus': UserPlus,
  'calendar-plus': CalendarPlus,
  'settings-2': Settings2,
  'file-plus': FilePlus,
};

export function resolveNavIcon(name: NavIconName): LucideIcon {
  return NAV_ICONS[name];
}
