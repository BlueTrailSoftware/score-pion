export interface ApiResponse<T> {
  status: string;
  message: string;
  data?: T;
}

export interface PagedData<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export function emptyPagedData<T>(pageSize: number): PagedData<T> {
  return { items: [], total: 0, page: 0, pageSize, totalPages: 0 };
}
