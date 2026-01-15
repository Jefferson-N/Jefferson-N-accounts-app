import { inject } from '@angular/core';
import {
  HttpRequest,
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { throwError } from 'rxjs';
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

      switch (error.status) {
        case 0:
          userMessage = 'No se pudo conectar con el servidor. Verifique su conexión.';
          break;

        case 400:
          userMessage = error.error?.message || 'Datos inválidos. Verifique los campos del formulario.';
          break;

        case 404:
          userMessage = error.error?.message || 'El recurso solicitado no fue encontrado.';
          break;

        case 409:
          userMessage = error.error?.message || 'El recurso ya existe o hay un conflicto.';
          break;

        case 500:
        case 502:
        case 503:
          userMessage = error.error?.message || 'Error del servidor. Por favor contacte con el administrador del sistema.';
          break;

        default:
          userMessage = error.error?.message || userMessage;
          break;
      }

      console.log('Mostrando notificación:', userMessage);
      notificationService.error(userMessage, 5000);
      return throwError(() => new Error(userMessage));
    })
  );
};
