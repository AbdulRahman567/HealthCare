import { NextResponse, type NextRequest } from 'next/server';

import { SESSION_FLAG_COOKIE } from '@/lib/session-constants';

const PROTECTED_PREFIXES = ['/app'];
const AUTH_PAGES = [
  '/login',
  '/register/hospital',
  '/forgot-password',
  '/reset-password',
  '/verify-email',
  '/resend-verification',
];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const hasSession = Boolean(request.cookies.get(SESSION_FLAG_COOKIE)?.value);

  const isProtected = PROTECTED_PREFIXES.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`),
  );
  const isAuthPage = AUTH_PAGES.some(
    (page) => pathname === page || pathname.startsWith(`${page}/`),
  );

  if (isProtected && !hasSession) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('next', pathname);
    return NextResponse.redirect(loginUrl);
  }

  if (isAuthPage && hasSession) {
    return NextResponse.redirect(new URL('/app', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/app/:path*',
    '/login',
    '/register/hospital',
    '/forgot-password',
    '/reset-password',
    '/verify-email',
    '/verify-email/:path*',
    '/resend-verification',
  ],
};
