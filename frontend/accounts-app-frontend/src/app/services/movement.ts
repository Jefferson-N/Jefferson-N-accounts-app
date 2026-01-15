import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Movimiento, PaginatedResponse } from './models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Movement {
  private apiUrl = `${environment.apiUrl}/movimientos`;

  constructor(private http: HttpClient) {}

  listar(page: number = 0, size: number = 10, cuentaId?: string, from?: string, to?: string): Observable<PaginatedResponse<Movimiento>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (cuentaId) {
      params = params.set('cuentaId', cuentaId);
    }
    if (from) {
      params = params.set('from', from);
    }
    if (to) {
      params = params.set('to', to);
    }

    return this.http.get<PaginatedResponse<Movimiento>>(this.apiUrl, { params });
  }

  obtener(id: string): Observable<Movimiento> {
    return this.http.get<Movimiento>(`${this.apiUrl}/${id}`);
  }

  crear(movimiento: any): Observable<Movimiento> {
    return this.http.post<Movimiento>(this.apiUrl, movimiento);
  }

  actualizar(id: string, movimiento: Movimiento): Observable<Movimiento> {
    return this.http.put<Movimiento>(`${this.apiUrl}/${id}`, movimiento);
  }

  actualizarParcial(id: string, movimiento: Partial<Movimiento>): Observable<Movimiento> {
    return this.http.patch<Movimiento>(`${this.apiUrl}/${id}`, movimiento);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  listarPorCuenta(cuentaId: string, page: number = 0, size: number = 10): Observable<PaginatedResponse<Movimiento>> {
    return this.http.get<PaginatedResponse<Movimiento>>(`${this.apiUrl}`, { 
      params: new HttpParams()
        .set('cuentaId', cuentaId)
        .set('page', page.toString())
        .set('size', size.toString())
    });
  }

  listarPorFechas(fechaInicio: string, fechaFin: string, page: number = 0, size: number = 10): Observable<PaginatedResponse<Movimiento>> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PaginatedResponse<Movimiento>>(`${this.apiUrl}/rango-fechas`, { params });
  }
}
