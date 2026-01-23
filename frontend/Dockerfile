# ----------------------
# Stage 1: Build Angular
# ----------------------
FROM node:20-alpine AS build

WORKDIR /app

# Copiar package.json y package-lock.json primero (para cachear npm install)
COPY package*.json ./
RUN npm install

# Copiar el resto del código y hacer build de Angular
COPY . .
RUN npm run build -- --configuration production

# ----------------------
# Stage 2: Nginx
# ----------------------
FROM nginx:alpine

# Limpiar la carpeta html por defecto
RUN rm -rf /usr/share/nginx/html/*

# Copiar el build de Angular desde el stage anterior
COPY --from=build /app/dist/frontend /usr/share/nginx/html

# Copiar configuración personalizada de nginx si tienes SPA fallback
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer el puerto
EXPOSE 80

# Comando por defecto para iniciar nginx
CMD ["nginx", "-g", "daemon off;"]
