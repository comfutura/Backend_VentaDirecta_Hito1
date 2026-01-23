# ----------------------
# Stage 1: Build Angular
# ----------------------
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci   # o npm install --legacy-peer-deps si hay conflictos

COPY . .
RUN npm run build -- --configuration production   # <-- usa esto, no --prod

# ----------------------
# Stage 2: Nginx
# ----------------------
FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

# ¡Aquí el cambio importante!
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Tu nginx.conf (asegúrate que esté en la raíz del proyecto frontend)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
