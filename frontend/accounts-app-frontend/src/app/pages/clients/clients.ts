import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, BehaviorSubject } from 'rxjs';
import { switchMap, shareReplay, tap, map } from 'rxjs/operators';
import { Client } from '../../services/client';
import { NotificationService } from '../../services/notification';
import { Cliente, CustomerRequest } from '../../services/models';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clients.html',
  styleUrl: './clients.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Clients implements OnInit {
  searchTerm = '';
  clients$!: Observable<Cliente[]>;
  showPassword = false;
  pagination$ = new BehaviorSubject<{ currentPage: number; pageSize: number; totalPages: number; totalRecords: number }>({
    currentPage: 1,
    pageSize: 10,
    totalPages: 1,
    totalRecords: 0
  });

  showModal = false;
  modalMode: 'create' | 'edit' = 'create';
  selectedClientId: string = '';

  formData: CustomerRequest = {
    name: '',
    gender: '',
    age: 0,
    identification: '',
    address: '',
    phone: '',
    password: '',
    status: true
  };

  get currentPage(): number {
    return this.pagination$.value.currentPage;
  }

  get pageSize(): number {
    return this.pagination$.value.pageSize;
  }

  get totalPages(): number {
    return this.pagination$.value.totalPages;
  }

  get totalRecords(): number {
    return this.pagination$.value.totalRecords;
  }

  constructor(
    private clientService: Client,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.clients$ = this.pagination$.pipe(
      switchMap(pagination =>
        this.clientService.listar(pagination.currentPage - 1, pagination.pageSize, this.searchTerm).pipe(
          tap((response: any) => {
            const newPagination = {
              currentPage: pagination.currentPage,
              pageSize: pagination.pageSize,
              totalPages: response.totalPages || 1,
              totalRecords: response.totalElements || 0
            };
            
            if (newPagination.totalPages !== pagination.totalPages || 
                newPagination.totalRecords !== pagination.totalRecords) {
              // Usar distinctUntilChanged en pagination$ para evitar actualizaciones innecesarias
              this.pagination$.next(newPagination);
            }
            this.cdr.markForCheck();
          }),
          map((response: any) => response.content || [])
        )
      ),
      shareReplay(1)
    );
    
    this.clients$.subscribe({
      error: (err: any) => {
        console.error('Error loading clients:', err);
        this.cdr.markForCheck();
      }
    });
  }

  onSearch(): void {
    this.pagination$.next({ ...this.pagination$.value, currentPage: 1 });
    this.loadClients();
  }

  onNewClient(): void {
    this.modalMode = 'create';
    this.formData = {
      name: '',
      gender: '',
      age: 0,
      identification: '',
      address: '',
      phone: '',
      password: '',
      status: true
    };
    this.showModal = true;
    this.cdr.markForCheck();
  }

  onEditClient(id: string): void {
    this.clientService.obtener(id).subscribe({
      next: (client: Cliente) => {
        this.modalMode = 'edit';
        this.selectedClientId = id;
        this.formData = {
          name: client.name,
          gender: client.gender,
          age: client.age,
          identification: client.identification,
          address: client.address,
          phone: client.phone,
          password: client.password,
          status: client.status
        };
        this.showModal = true;
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        this.notificationService.error('Error al cargar los detalles del cliente');
        console.error('Error loading client details:', err);
      }
    });
  }

  onDeleteClient(id: string): void {
    if (confirm('¿Está seguro de que desea eliminar este cliente?')) {
      this.clientService.eliminar(id).subscribe({
        next: () => {
          this.notificationService.success('Cliente eliminado correctamente');
          this.loadClients();
        },
        error: () => {
          // El interceptor ya mostró la notificación de error
          this.cdr.markForCheck();
        }
      });
    }
  }

  onSaveClient(): void {
    // Validaciones
    const errors = this.validateClientForm();
    if (errors.length > 0) {
      const errorMessage = errors.join('\n');
      this.notificationService.error(errorMessage, 6000);
      return;
    }

    if (this.modalMode === 'create') {
      this.clientService.crear(this.formData as Cliente).subscribe({
        next: () => {
          this.notificationService.success('Cliente creado correctamente');
          this.showModal = false;
          this.loadClients();
          this.cdr.markForCheck();
        },
        error: () => {
          // El interceptor ya mostró la notificación de error
          this.cdr.markForCheck();
        }
      });
    } else {
      this.clientService.actualizar(this.selectedClientId, this.formData as Cliente).subscribe({
        next: () => {
          this.notificationService.success('Cliente actualizado correctamente');
          this.showModal = false;
          this.loadClients();
          this.cdr.markForCheck();
        },
        error: () => {
          // El interceptor ya mostró la notificación de error
          this.cdr.markForCheck();
        }
      });
    }
  }

  validateClientForm(): string[] {
    const errors: string[] = [];

    if (!this.formData.name || this.formData.name.trim().length < 8) {
      errors.push('El nombre debe tener al menos 8 caracteres');
    }
    if (this.formData.name && this.formData.name.length > 30) {
      errors.push('El nombre no puede exceder 30 caracteres');
    }

    if (!this.formData.gender) {
      errors.push('El género es requerido');
    }

    if (!this.formData.age || this.formData.age < 18) {
      errors.push('Debe ser mayor de 18 años');
    }
    if (this.formData.age > 120) {
      errors.push('La edad no puede ser mayor a 120 años');
    }

    if (!this.formData.identification || !/^\d{10}$/.test(this.formData.identification)) {
      errors.push('La cédula debe contener 10 dígitos');
    }

    if (!this.formData.address || this.formData.address.trim().length < 5) {
      errors.push('La dirección debe tener al menos 5 caracteres');
    }
    if (this.formData.address && this.formData.address.length > 100) {
      errors.push('La dirección no puede exceder 100 caracteres');
    }

    if (!this.formData.phone || !/^\d{10}$/.test(this.formData.phone)) {
      errors.push('El teléfono debe contener 10 dígitos');
    }

    if (!this.formData.password || this.formData.password.length < 8) {
      errors.push('La contraseña debe tener al menos 8 caracteres');
    }

    return errors;
  }

  onCloseModal(): void {
    this.showModal = false;
    this.cdr.markForCheck();
  }

  onPreviousPage(): void {
    if (this.currentPage > 1) {
      this.pagination$.next({ ...this.pagination$.value, currentPage: this.currentPage - 1 });
      this.loadClients();
    }
  }

  onNextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.pagination$.next({ ...this.pagination$.value, currentPage: this.currentPage + 1 });
      this.loadClients();
    }
  }
}

