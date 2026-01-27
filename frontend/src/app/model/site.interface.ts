// src/app/models/site.interface.ts
export interface Site {
  idSite?: number; // Optional for new creations
  codigoSitio?: string; //PUEDE SER NULL
  descripcion: string;//OBLIGATORIO
  activo?: boolean;
}
