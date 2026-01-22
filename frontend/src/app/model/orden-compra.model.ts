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
  estadoOcNombre: string;
  otsNombre: string;          // ya viene concatenado: "OT: 20250001 - Backbone fibra..."
  maestroCodigo: string;
  proveedorNombre: string;
  cantidad: number;
  costoUnitario: number;
  fechaOc: string;            // ISO string desde backend (LocalDateTime)
  observacion?: string;

  // Opcional: calcular total en frontend si lo necesitas
  total?: number;             // cantidad * costoUnitario (puedes calcularlo)
}

// Para la paginación (respuesta del backend)
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
