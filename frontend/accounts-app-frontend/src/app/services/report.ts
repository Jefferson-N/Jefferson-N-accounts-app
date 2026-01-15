import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Report {
  private apiUrl = `${environment.apiUrl}/reportes`;

  constructor(private http: HttpClient) {}

  generarReporte(clienteId: string, fechaInicio: string, fechaFin: string): Observable<any> {
    const params = new HttpParams()
      .set('clienteId', clienteId)
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);

    return this.http.get<any>(`${this.apiUrl}`, { params });
  }

  generarJSON(clienteId: string, fechaInicio: string, fechaFin: string): Observable<any> {
    const params = new HttpParams()
      .set('clienteId', clienteId)
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);

    return this.http.get<any>(`${this.apiUrl}/json`, { params });
  }

  generarPDF(clienteId: string, fechaInicio: string, fechaFin: string): Observable<Blob> {
    const params = new HttpParams()
      .set('clienteId', clienteId)
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);

    return this.http.get(`${this.apiUrl}/pdf`, { 
      params, 
      responseType: 'blob' 
    });
  }

  descargarPDF(clienteId: string, fechaInicio: string, fechaFin: string): Observable<Blob> {
    return this.generarPDF(clienteId, fechaInicio, fechaFin);
  }
}
