# ================= BUILD ANGULAR =================
FROM node:20-alpine AS build

WORKDIR /app

# Copiamos package.json y package-lock.json
COPY package*.json ./

# Instalamos dependencias
RUN npm install

# Copiamos todo el proyecto
COPY . .

# Generamos el build de Angular en modo producción
RUN npm run build -- --configuration production

# ================= RUN IN NGINX =================
FROM nginx:alpine

# Limpiamos el contenido por defecto de nginx
RUN rm -rf /usr/share/nginx/html/*

# Copiamos el build de Angular
COPY --from=build /app/dist/frontend /usr/share/nginx/html

# Copiamos configuración custom de Nginx para SPA
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponemos el puerto 80
EXPOSE 80

# Iniciamos Nginx
CMD ["nginx", "-g", "daemon off;"]
