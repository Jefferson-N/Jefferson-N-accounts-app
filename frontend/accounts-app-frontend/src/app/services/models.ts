export interface Persona {
  id?: string;
  name: string;
  gender: string;
  age: number;
  identification: string;
  address: string;
  phone: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Cliente extends Persona {
  password: string;
  status: boolean;
}

export interface CustomerRequest {
  name: string;
  gender: string;
  age: number;
  identification: string;
  address: string;
  phone: string;
  password: string;
  status: boolean;
}

export interface CustomerUpdateRequest {
  name?: string;
  gender?: string;
  age?: number;
  identification?: string;
  address?: string;
  phone?: string;
  password?: string;
  status?: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Cuenta {
  id?: string;
  accountNumber: string;
  accountType: 'AHORRO' | 'CORRIENTE';
  initialBalance: number;
  balance?: number;
  status: boolean;
  customerId?: string;
  customer?: Cliente;
  createdAt?: string;
  updatedAt?: string;
}

export interface AccountRequest {
  accountNumber: string;
  accountType: 'AHORRO' | 'CORRIENTE';
  initialBalance: number;
  status: boolean;
  customerId: string;
}

export interface AccountUpdateRequest {
  accountNumber?: string;
  accountType?: 'AHORRO' | 'CORRIENTE';
  initialBalance?: number;
  status?: boolean;
}

export interface Movimiento {
  id?: string;
  date: string;
  type: 'CREDITO' | 'DEBITO';
  amount: number;
  balance?: number;
  accountId?: string;
  account?: Cuenta;
  createdAt?: string;
  updatedAt?: string;
}

export interface TransactionRequest {
  date: string;
  transactionType: 'CREDITO' | 'DEBITO';
  amount: number;
  accountId: string;
}

export interface TransactionUpdateRequest {
  date?: string;
  type?: 'CREDITO' | 'DEBITO';
  amount?: number;
  balance?: number;
}
