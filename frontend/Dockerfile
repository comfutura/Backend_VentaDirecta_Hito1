# ================= RUN IN NGINX =================
FROM nginx:alpine

# Limpiar contenido viejo
RUN rm -rf /usr/share/nginx/html/*

# Copiar build Angular generado localmente
COPY dist/frontend/ /usr/share/nginx/html/

# Copiar nginx custom para SPA fallback
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer puerto 80
EXPOSE 80

# Ejecutar Nginx en foreground
CMD ["nginx", "-g", "daemon off;"]
