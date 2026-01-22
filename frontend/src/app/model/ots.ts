// src/app/core/models/ots.ts

// Respuesta principal (listado y detalle)
export interface OtResponse {
  idOts: number;
  ot: number;
  descripcion: string;
  fechaApertura?: string | null;       // ISO date string o null
  diasAsignados: number;               // calculado en backend
  activo: boolean;
  fechaCreacion: string;               // ISO timestamp

  // Relaciones con nombres
  clienteRazonSocial?: string | null;
  areaNombre?: string | null;
  proyectoNombre?: string | null;
  faseNombre?: string | null;
  siteNombre?: string | null;          // ahora es codigo_sitio
  regionNombre?: string | null;

  // Responsables del cliente
  jefaturaClienteSolicitante?: string | null;
  analistaClienteSolicitante?: string | null;

  // Responsables internos (nombres completos)
  coordinadorTiCw?: string | null;
  jefaturaResponsable?: string | null;
  liquidador?: string | null;
  ejecutante?: string | null;
  analistaContable?: string | null;

  // Estado
  estadoOt?: string | null;

  // Trabajadores asignados
  trabajadoresAsignados: TrabajadorEnOtDto[];
}

export interface TrabajadorEnOtDto {
  idTrabajador: number;
  nombresCompletos: string;
  cargoNombre?: string | null;
  areaNombre?: string | null;
  rolEnOt: string;
  activo?: boolean;
}

// Request para crear/editar OT completa
export interface CrearOtCompletaRequest {
  ot: OtCreateRequest;
  trabajadores?: Array<{ idTrabajador: number; rolEnOt: string }>;
  detalles?: Array<OtDetalleRequest>; // si usas ítems/materiales
}

export interface OtCreateRequest {
  idOts?: number;                      // para edición
  idOtsAnterior?: number | null;
  idCliente: number;
  idArea: number;
  idProyecto: number;
  idFase: number;
  idSite: number;
  idRegion: number;
  descripcion?: string;
  fechaApertura?: string;              // 'YYYY-MM-DD'
  idJefaturaClienteSolicitante?: number | null;
  idAnalistaClienteSolicitante?: number | null;
  idCoordinadorTiCw?: number | null;
  idJefaturaResponsable?: number | null;
  idLiquidador?: number | null;
  idEjecutante?: number | null;
  idAnalistaContable?: number | null;
}

export interface OtDetalleRequest {
  idMaestro: number;
  idProveedor: number;
  cantidad: number;
  precioUnitario: number;
}

// Para paginación genérica
export interface Page<T> {
  content: T[];
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
