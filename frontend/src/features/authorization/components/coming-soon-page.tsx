import Link from 'next/link';

import { buttonVariants } from '@/components/ui/button';
import { cn } from '@/lib/utils';

type ComingSoonPageProps = {
  title: string;
  description: string;
};

export function ComingSoonPage({ title, description }: ComingSoonPageProps) {
  return (
    <section className="mx-auto max-w-lg space-y-4 rounded-xl border p-6">
      <p className="text-muted-foreground text-xs font-medium tracking-wide uppercase">Coming soon</p>
      <div className="space-y-2">
        <h1 className="text-xl font-semibold tracking-tight">{title}</h1>
        <p className="text-muted-foreground text-sm">{description}</p>
      </div>
      <Link href="/app" className={cn(buttonVariants({ variant: 'outline' }), 'h-9')}>
        Back to dashboard
      </Link>
    </section>
  );
}
