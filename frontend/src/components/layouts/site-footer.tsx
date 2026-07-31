import { HeartPulse } from 'lucide-react';
import Link from 'next/link';

const footerLinks = [
  { href: '/pricing', label: 'Pricing' },
  { href: '/login', label: 'Sign in' },
  { href: '/register/hospital', label: 'Register hospital' },
];

export function SiteFooter() {
  const year = new Date().getFullYear();

  return (
    <footer className="border-t border-black/[0.04] bg-white/40">
      <div className="mx-auto max-w-6xl px-6 py-12">
        <div className="flex flex-col items-center gap-8 md:flex-row md:items-start md:justify-between">
          {/* Brand */}
          <div className="flex flex-col items-center gap-2 md:items-start">
            <Link
              href="/"
              className="flex items-center gap-2.5 transition-opacity hover:opacity-80"
            >
              <span className="flex size-8 items-center justify-center rounded-lg bg-primary text-white shadow-xs">
                <HeartPulse className="size-3.5" aria-hidden />
              </span>
              <span className="text-sm font-semibold tracking-wide">Healthcare HMS</span>
            </Link>
            <p className="text-xs text-muted-foreground">
              The operating system for modern hospitals.
            </p>
          </div>

          {/* Navigation */}
          <div className="flex flex-col items-center gap-3 md:items-end">
            <nav className="flex items-center gap-6">
              {footerLinks.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                >
                  {link.label}
                </Link>
              ))}
            </nav>
            <p className="text-[11px] text-muted-foreground/60">
              &copy; {year} Healthcare HMS. All rights reserved.
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
}
