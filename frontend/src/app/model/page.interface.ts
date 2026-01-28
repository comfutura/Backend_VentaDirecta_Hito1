// src/app/core/model/ots.ts
export interface PageResponseDTO<T> {
  content: T[];
  currentPage: number;           // En lugar de "number" (0-based)
  totalItems: number;           // En lugar de "totalElements"
  totalPages: number;           // En lugar de "totalPages"
  first: boolean;
  last: boolean;
  pageSize: number;            // En lugar de "size"
}

// Cambia Page por PageResponseDTO o usa un alias
export type Page<T> = PageResponseDTO<T>;
