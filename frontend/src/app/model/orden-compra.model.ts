// src/app/models/orden-compra.model.ts

export interface OrdenCompraRequest {
  estadoOcId: number;
  otsId: number;
  maestroId: number;
  proveedorId: number;
  cantidad: number;           // o string si prefieres manejar como string en formulario
  costoUnitario: number;
  observacion?: string;
}

export interface OrdenCompraResponse {
  idOc: number;

  estadoOcId:   number;
  estadoOcNombre: string;

  otsId:        number;
  otsNombre:    string;

  maestroId:    number;
  maestroCodigo: string;

  proveedorId:  number;
  proveedorNombre: string;
  // ──────────────────────────────────────────────────────────────────────

  cantidad: number;
  costoUnitario: number;
  fechaOc: string;
  observacion?: string;

  total?: number;  // opcional
}

export interface PageOrdenCompra {
  content: OrdenCompraResponse[];
  pageable: {
    sort: { sorted: boolean; unsorted: boolean; empty: boolean };
    offset: number;
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: { sorted: boolean; unsorted: boolean; empty: boolean };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
