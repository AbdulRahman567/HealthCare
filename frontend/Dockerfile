# Build context must be the monorepo root (npm workspaces lockfile lives there).
FROM node:22-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
COPY frontend/package.json ./frontend/
RUN npm ci --workspace=frontend

FROM node:22-alpine AS builder
WORKDIR /app

ARG NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
ARG NEXT_PUBLIC_APP_NAME=Healthcare HMS
ARG NEXT_PUBLIC_ENV=development

ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_APP_NAME=$NEXT_PUBLIC_APP_NAME
ENV NEXT_PUBLIC_ENV=$NEXT_PUBLIC_ENV

COPY --from=deps /app/node_modules ./node_modules
COPY --from=deps /app/frontend/node_modules ./frontend/node_modules
COPY frontend ./frontend
WORKDIR /app/frontend
RUN npm run build

FROM node:22-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

RUN addgroup -S hms && adduser -S -G hms hms

# With outputFileTracingRoot at the monorepo root, standalone nests under /frontend.
COPY --from=builder --chown=hms:hms /app/frontend/.next/standalone ./
COPY --from=builder --chown=hms:hms /app/frontend/.next/static ./frontend/.next/static
COPY --from=builder --chown=hms:hms /app/frontend/public ./frontend/public

USER hms
EXPOSE 3000
CMD ["node", "frontend/server.js"]
