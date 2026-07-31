'use client';

import { ArrowRight, HeartPulse, Menu, X } from 'lucide-react';
import Link from 'next/link';
import { useState } from 'react';

import { buttonVariants } from '@/components/ui/button';
import { cn } from '@/lib/utils';

const navLinks = [
  { href: '/pricing', label: 'Pricing' },
  { href: '/login', label: 'Sign in' },
];

export function SiteHeader() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <header className="sticky top-0 z-50 border-b border-black/[0.03] bg-white/80 backdrop-blur-md supports-[backdrop-filter]:bg-white/60">
      <div className="mx-auto flex h-16 w-full max-w-6xl items-center justify-between px-6">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-2.5 transition-opacity hover:opacity-80">
          <span className="flex size-9 items-center justify-center rounded-xl bg-primary text-white shadow-xs">
            <HeartPulse className="size-4" aria-hidden />
          </span>
          <span className="text-sm font-semibold tracking-wide">Healthcare HMS</span>
        </Link>

        {/* Desktop nav */}
        <nav className="hidden items-center gap-2 md:flex">
          {navLinks.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={cn(
                buttonVariants({ variant: 'ghost' }),
                'h-9 rounded-lg px-3.5 text-sm font-medium transition-colors',
              )}
            >
              {link.label}
            </Link>
          ))}
          <Link
            href="/register/hospital"
            className={cn(
              buttonVariants({ variant: 'default' }),
              'group h-9 gap-1.5 rounded-lg px-4 text-sm font-medium shadow-xs',
            )}
          >
            Register hospital
            <ArrowRight className="size-3.5 transition-transform duration-200 group-hover:translate-x-0.5" />
          </Link>
        </nav>

        {/* Mobile hamburger */}
        <button
          type="button"
          onClick={() => setMobileOpen(!mobileOpen)}
          className={cn(
            'flex size-9 items-center justify-center rounded-lg md:hidden',
            'text-muted-foreground hover:bg-muted hover:text-foreground transition-colors',
          )}
          aria-label={mobileOpen ? 'Close menu' : 'Open menu'}
        >
          {mobileOpen ? <X className="size-5" /> : <Menu className="size-5" />}
        </button>
      </div>

      {/* Mobile nav */}
      {mobileOpen && (
        <div className="border-t border-black/[0.04] bg-white/95 backdrop-blur-md md:hidden">
          <nav className="mx-auto flex max-w-6xl flex-col gap-2 px-6 py-4">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                onClick={() => setMobileOpen(false)}
                className={cn(
                  buttonVariants({ variant: 'ghost' }),
                  'h-10 justify-start rounded-lg text-sm font-medium',
                )}
              >
                {link.label}
              </Link>
            ))}
            <Link
              href="/register/hospital"
              onClick={() => setMobileOpen(false)}
              className={cn(
                buttonVariants({ variant: 'default' }),
                'h-10 justify-center gap-1.5 rounded-lg text-sm font-medium',
              )}
            >
              Register hospital
              <ArrowRight className="size-3.5" />
            </Link>
          </nav>
        </div>
      )}
    </header>
  );
}
