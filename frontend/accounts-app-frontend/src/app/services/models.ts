export interface Persona {
  id?: string;
  nombre: string;
  genero: string;
  edad: number;
  identificacion: string;
  direccion: string;
  telefono: string;
}

export interface Cliente extends Persona {
  clienteId?: string;
  contrasena: string;
  estado: boolean;
}

export interface CustomerCreate {
  nombre: string;
  genero: string;
  edad: number;
  identificacion: string;
  direccion: string;
  telefono: string;
  contrasena: string;
  estado: boolean;
}

export interface CustomerUpdate {
  nombre?: string;
  genero?: string;
  edad?: number;
  identificacion?: string;
  direccion?: string;
  telefono?: string;
  contrasena?: string;
  estado?: boolean;
}

export interface CustomerPatch {
  nombre?: string;
  genero?: string;
  edad?: number;
  identificacion?: string;
  direccion?: string;
  telefono?: string;
  contrasena?: string;
  estado?: boolean;
}

export interface CustomerDTO extends Cliente {
  id: string;
}

export interface Cuenta {
  id?: string;
  numeroCuenta: string;
  tipoCuenta: 'AHORRO' | 'CORRIENTE';
  saldoInicial: number;
  estado: boolean;
  customerId?: string;
  cliente?: Cliente;
}

export interface AccountCreate {
  numeroCuenta: string;
  tipoCuenta: 'AHORRO' | 'CORRIENTE';
  saldoInicial: number;
  estado: boolean;
  customerId: string;
}

export interface AccountPatch {
  estado?: boolean;
}

export interface AccountDTO extends Cuenta {
  id: string;
  saldoDisponible: number;
}

export interface Movimiento {
  id?: string;
  fecha: Date;
  tipoMovimiento: 'CREDITO' | 'DEBITO';
  valor: number;
  saldo: number;
  cuentaId?: string;
  cuenta?: Cuenta;
}

export interface TransactionCreate {
  tipoMovimiento: 'CREDITO' | 'DEBITO';
  valor: number;
  cuentaId: string;
}

export interface TransactionDTO extends Movimiento {
  id: string;
}

export interface Reporte {
  cliente: ClienteReporte;
  cuentas: CuentaReporte[];
  fechaInicio: Date;
  fechaFin: Date;
  totalDebitos: number;
  totalCreditos: number;
}

export interface ClienteReporte {
  id: string;
  nombre: string;
  identificacion: string;
}

export interface CuentaReporte {
  id: string;
  numeroCuenta: string;
  tipoCuenta: string;
  saldoInicial: number;
  saldoDisponible: number;
}

export interface PaginationMetadata {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface PageResponse<T> {
  content: T[];
  metadata: PaginationMetadata;
}

export interface ApiError {
  message: string;
  code?: string;
  timestamp?: Date;
}
