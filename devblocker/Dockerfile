# Stage 1: Build
FROM node:20-alpine AS builder
WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# Stage 2: Runtime
FROM node:20-alpine
WORKDIR /app

RUN npm install -g serve
COPY --from=builder /build/dist ./dist

EXPOSE 3000
CMD ["serve", "-s", "dist", "-l", "3000"]
