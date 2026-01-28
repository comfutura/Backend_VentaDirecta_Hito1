// src/app/models/page.interface.ts
export interface Page<T> {
  content: T[];
  number: number;           // página actual (0-based)
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// src/app/models/page.interface.ts
export interface PageResponse<T> {
  content: T[];
  page: {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}

