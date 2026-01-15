import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
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
      .set('from', fechaInicio)
      .set('to', fechaFin)
      .set('format', 'json');

    return this.http.get<any>(`${this.apiUrl}`, { params });
  }

  generarJSON(clienteId: string, fechaInicio: string, fechaFin: string): Observable<any> {
    const params = new HttpParams()
      .set('clienteId', clienteId)
      .set('from', fechaInicio)
      .set('to', fechaFin)
      .set('format', 'json');

    return this.http.get<any>(`${this.apiUrl}`, { params });
  }

  generarPDF(clienteId: string, fechaInicio: string, fechaFin: string): Observable<Blob> {
    const params = new HttpParams()
      .set('clienteId', clienteId)
      .set('from', fechaInicio)
      .set('to', fechaFin)
      .set('format', 'pdf');

    return this.http.get<any>(`${this.apiUrl}`, { params }).pipe(
      map((response: any) => {
        // El servidor devuelve {"base64": "..."}, necesitamos decodificar
        if (response.base64) {
          const binaryString = atob(response.base64);
          const bytes = new Uint8Array(binaryString.length);
          for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
          }
          return new Blob([bytes], { type: 'application/pdf' });
        }
        return response;
      })
    );
  }

  descargarPDF(clienteId: string, fechaInicio: string, fechaFin: string): Observable<Blob> {
    return this.generarPDF(clienteId, fechaInicio, fechaFin);
  }
}
