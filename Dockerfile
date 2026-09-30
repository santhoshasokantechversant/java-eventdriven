# --- Stage 1: Build ---
FROM node:20-alpine AS build

# Set working directory
WORKDIR /app

# Accept build argument for Vite environment variable
ARG VITE_BACKEND_URL
ENV VITE_BACKEND_URL=${VITE_BACKEND_URL}

# Copy package.json and package-lock.json / pnpm-lock.yaml if exists
COPY package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps

# Copy all source files
COPY . .

# Build production-ready static files
RUN npm run build

# --- Stage 2: Run ---
FROM nginx:stable-alpine

# Copy build output from previous stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom nginx config (optional)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
