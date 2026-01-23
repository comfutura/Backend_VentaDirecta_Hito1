# Stage 1: build Angular
FROM node:20-alpine as build

WORKDIR /app

# Copiar package.json y package-lock.json
COPY package*.json ./

RUN npm install

# Copiar todo el proyecto
COPY . .

# Generar build de Angular
RUN npx ng build --configuration production

# Stage 2: servir con Nginx
FROM nginx:alpine

# Limpiar html viejo
RUN rm -rf /usr/share/nginx/html/*

# Copiar build Angular generado en stage 1
COPY --from=build /app/dist/frontend/ /usr/share/nginx/html/

# Copiar nginx custom si tienes SPA fallback
# COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
