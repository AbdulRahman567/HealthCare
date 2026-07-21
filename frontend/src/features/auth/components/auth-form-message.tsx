import { AlertCircleIcon, CheckCircle2Icon } from 'lucide-react';

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';

type AuthFormMessageProps = {
  variant: 'error' | 'success';
  title: string;
  description?: string;
};

export function AuthFormMessage({ variant, title, description }: AuthFormMessageProps) {
  const isError = variant === 'error';

  return (
    <Alert variant={isError ? 'destructive' : 'default'} className={isError ? undefined : 'border-emerald-200 bg-emerald-50 text-emerald-950'}>
      {isError ? <AlertCircleIcon /> : <CheckCircle2Icon className="text-emerald-600" />}
      <AlertTitle>{title}</AlertTitle>
      {description ? <AlertDescription>{description}</AlertDescription> : null}
    </Alert>
  );
}
