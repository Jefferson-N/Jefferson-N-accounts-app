import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cuenta, PaginatedResponse } from './models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Account {
  private apiUrl = `${environment.apiUrl}/cuentas`;

  constructor(private http: HttpClient) {}

  listar(page: number = 0, size: number = 10, search: string = ''): Observable<PaginatedResponse<Cuenta>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('q', search);
    }

    return this.http.get<PaginatedResponse<Cuenta>>(this.apiUrl, { params });
  }

  obtener(id: string): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.apiUrl}/${id}`);
  }

  crear(cuenta: Cuenta): Observable<Cuenta> {
    return this.http.post<Cuenta>(this.apiUrl, cuenta);
  }

  actualizar(id: string, cuenta: Cuenta): Observable<Cuenta> {
    return this.http.put<Cuenta>(`${this.apiUrl}/${id}`, cuenta);
  }

  actualizarParcial(id: string, cuenta: Partial<Cuenta>): Observable<Cuenta> {
    return this.http.patch<Cuenta>(`${this.apiUrl}/${id}`, cuenta);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  listarPorCliente(clienteId: string, page: number = 0, size: number = 10): Observable<PaginatedResponse<Cuenta>> {
    return this.http.get<PaginatedResponse<Cuenta>>(`${this.apiUrl}`, {
      params: new HttpParams()
        .set('clienteId', clienteId)
        .set('page', page.toString())
        .set('size', size.toString())
    });
  }
}
