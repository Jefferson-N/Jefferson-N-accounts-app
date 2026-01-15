import { Injectable, inject } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationService } from './notification';

export const httpErrorInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next) => {
  const notificationService = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('Error HTTP interceptado:', {
        status: error.status,
        message: error.message,
        errorBody: error.error
      });

      let userMessage = 'Ocurrió un error procesando la solicitud';

      if (error.status === 0) {
        userMessage = 'No se pudo conectar con el servidor. Verifique su conexión.';
      } else if (error.status === 400) {
        userMessage = error.error?.message || 'Datos inválidos. Verifique los campos del formulario.';
      } else if (error.status === 404) {
        userMessage = error.error?.message || 'El recurso solicitado no fue encontrado.';
      } else if (error.status === 409) {
        userMessage = error.error?.message || 'El recurso ya existe o hay un conflicto.';
      } else if (error.status === 500 || error.status === 502 || error.status === 503) {
        userMessage = error.error?.message || 'Error del servidor. Por favor contacte con el administrador del sistema.';
      }

      console.log('Mostrando notificación:', userMessage);
      notificationService.error(userMessage, 8000);
      return throwError(() => new Error(userMessage));
    })
  );
};

