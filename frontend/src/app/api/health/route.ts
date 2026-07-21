import { NextResponse } from 'next/server';

export async function GET() {
  return NextResponse.json({
    status: 'UP',
    service: 'frontend',
    timestamp: new Date().toISOString(),
  });
}
