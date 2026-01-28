// src/app/shared/models/pagination.model.ts
export interface PageResponse<T> {
  content: T[];
  currentPage: number;           // Página actual (0-based en backend)
  totalItems: number;           // Total de elementos
  totalPages: number;           // Total de páginas
  first: boolean;
  last: boolean;
  pageSize: number;            // Elementos por página
}

export interface PaginationConfig {
  showInfo?: boolean;          // Mostrar info de registros
  showSizeSelector?: boolean;  // Mostrar selector de tamaño
  showNavigation?: boolean;    // Mostrar botones de navegación
  showJumpToPage?: boolean;    // Mostrar salto a página específica
  showPageNumbers?: boolean;   // Mostrar números de página
  pageSizes?: number[];        // Tamaños de página disponibles
  maxPageNumbers?: number;     // Máximo de números a mostrar
  align?: 'start' | 'center' | 'end'; // Alineación
  size?: 'sm' | 'md' | 'lg';   // Tamaño del paginador
}

export const DEFAULT_PAGINATION_CONFIG: PaginationConfig = {
  showInfo: true,
  showSizeSelector: true,
  showNavigation: true,
  showJumpToPage: true,
  showPageNumbers: true,
  pageSizes: [5, 10, 20, 50, 100],
  maxPageNumbers: 5,
  align: 'center',
  size: 'md'
};
