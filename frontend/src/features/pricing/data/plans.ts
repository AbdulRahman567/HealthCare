export type BillingInterval = 'monthly' | 'yearly';

export type PlanFeature = {
  text: string;
  included: boolean;
};

export type PlanPricing = {
  monthly: number | null; // null = custom pricing
  yearly: number | null;
};

export type Plan = {
  id: string;
  name: string;
  description: string;
  pricing: PlanPricing;
  trialDays: number | null;
  popular: boolean;
  cta: {
    label: string;
    href: string;
  };
  features: PlanFeature[];
  highlightedFeatures: string[];
};

export const PLANS: Plan[] = [
  {
    id: 'BASIC',
    name: 'Basic',
    description: 'Essential tools for small clinics just getting started.',
    pricing: { monthly: 0, yearly: 0 },
    trialDays: null,
    popular: false,
    cta: {
      label: 'Get started free',
      href: '/register/hospital?plan=BASIC',
    },
    features: [
      { text: 'Up to 5 staff accounts', included: true },
      { text: 'Up to 500 patient records', included: true },
      { text: 'Basic appointment scheduling', included: true },
      { text: 'Clinical notes & SOAP', included: true },
      { text: 'Email support', included: true },
      { text: 'Community access', included: true },
      { text: 'Billing & invoicing', included: false },
      { text: 'Advanced analytics', included: false },
      { text: 'API access', included: false },
      { text: 'Priority support', included: false },
    ],
    highlightedFeatures: [
      'Up to 5 staff accounts',
      'Up to 500 patient records',
      'Basic appointment scheduling',
    ],
  },
  {
    id: 'STANDARD',
    name: 'Standard',
    description: 'Perfect for growing hospitals with expanding teams.',
    pricing: { monthly: 99, yearly: 990 },
    trialDays: 14,
    popular: false,
    cta: {
      label: 'Start 14-day free trial',
      href: '/register/hospital?plan=STANDARD',
    },
    features: [
      { text: 'Up to 25 staff accounts', included: true },
      { text: 'Up to 5,000 patient records', included: true },
      { text: 'Calendar view & queue management', included: true },
      { text: 'Clinical notes & SOAP', included: true },
      { text: 'Billing & invoicing', included: true },
      { text: 'Basic analytics dashboard', included: true },
      { text: 'API access', included: true },
      { text: 'Email support', included: true },
      { text: 'Custom branding', included: false },
      { text: 'Dedicated account manager', included: false },
    ],
    highlightedFeatures: [
      'Up to 25 staff accounts',
      'Calendar view & queue management',
      'Billing & invoicing',
      'API access',
    ],
  },
  {
    id: 'PREMIUM',
    name: 'Premium',
    description: 'Full-featured platform for established healthcare organizations.',
    pricing: { monthly: 299, yearly: 2990 },
    trialDays: 14,
    popular: true,
    cta: {
      label: 'Start 14-day free trial',
      href: '/register/hospital?plan=PREMIUM',
    },
    features: [
      { text: 'Unlimited staff accounts', included: true },
      { text: 'Unlimited patient records', included: true },
      { text: 'Full appointment suite', included: true },
      { text: 'Clinical notes, SOAP & templates', included: true },
      { text: 'Advanced billing & insurance', included: true },
      { text: 'Advanced analytics & reports', included: true },
      { text: 'API access & webhooks', included: true },
      { text: 'Priority support (SLA 99.9%)', included: true },
      { text: 'Custom branding', included: true },
      { text: 'Dedicated account manager', included: false },
    ],
    highlightedFeatures: [
      'Unlimited staff & patients',
      'Full appointment suite',
      'Advanced billing & insurance',
      'Priority support (SLA 99.9%)',
    ],
  },
  {
    id: 'ENTERPRISE',
    name: 'Enterprise',
    description: 'Custom solutions for large healthcare systems and hospital groups.',
    pricing: { monthly: null, yearly: null },
    trialDays: null,
    popular: false,
    cta: {
      label: 'Contact sales',
      href: '/register/hospital?plan=ENTERPRISE',
    },
    features: [
      { text: 'Everything in Premium', included: true },
      { text: 'Unlimited staff & patients', included: true },
      { text: 'Multi-tenant hospital groups', included: true },
      { text: 'Custom integrations & HL7 FHIR', included: true },
      { text: 'Dedicated infrastructure', included: true },
      { text: 'Custom SLA (99.99% uptime)', included: true },
      { text: 'Dedicated account manager', included: true },
      { text: 'On-premise deployment option', included: true },
      { text: '24/7 phone & email support', included: true },
      { text: 'Custom feature development', included: true },
    ],
    highlightedFeatures: [
      'Multi-tenant hospital groups',
      'Custom integrations & HL7 FHIR',
      'Dedicated infrastructure',
      'On-premise deployment option',
    ],
  },
];

export function getPlanById(id: string): Plan | undefined {
  return PLANS.find((p) => p.id === id);
}

export function formatPrice(price: number | null): string {
  if (price === null) return 'Custom';
  if (price === 0) return 'Free';
  return `$${price.toLocaleString()}`;
}

export function getIntervalLabel(interval: BillingInterval): string {
  return interval === 'monthly' ? '/month' : '/year';
}
