import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, BehaviorSubject } from 'rxjs';
import { switchMap, shareReplay, tap, map } from 'rxjs/operators';
import { Client } from '../../services/client';
import { Account } from '../../services/account';
import { Movement } from '../../services/movement';
import { NotificationService } from '../../services/notification';
import { Cliente, CustomerRequest } from '../../services/models';
import { Reports } from '../reports/reports';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, FormsModule, Reports],
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
  
  // Modales adicionales
  showAccountsModal = false;
  showReportsModal = false;
  selectedClientName = '';
  
  // Modal de confirmación
  showConfirmDeleteModal = false;
  confirmDeleteMessage = '';
  confirmDeleteCallback: (() => void) | null = null;
  
  // Para la modal de cuentas y movimientos
  clientAccounts: any[] = [];
  accountMovements: any[] = [];
  selectedAccountId: string = '';
  showAccountFormModal = false;
  showMovementFormModal = false;
  currentClient: Cliente | null = null;

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

  accountFormData: any = {
    accountType: 'AHORRO',
    initialBalance: 0,
    status: true,
    customerId: ''
  };

  movementFormData: any = {
    description: '',
    amount: 0,
    transactionType: 'CREDITO',
    accountId: ''
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
    private accountService: Account,
    private movementService: Movement,
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
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.notificationService.error('Error al cargar los detalles del cliente');
        console.error('Error loading client details:', err);
      }
    });
  }

  onDeleteClient(id: string): void {
    this.confirmDeleteMessage = '¿Está seguro de que desea eliminar este cliente?';
    this.confirmDeleteCallback = () => {
      this.clientService.eliminar(id).subscribe({
        next: () => {
          this.notificationService.success('Cliente eliminado correctamente');
          this.showConfirmDeleteModal = false;
          this.loadClients();
        },
        error: () => {
          this.showConfirmDeleteModal = false;
          this.cdr.markForCheck();
        }
      });
    };
    this.showConfirmDeleteModal = true;
    this.cdr.markForCheck();
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

    if (!this.formData.password || !/^\d{4}$/.test(this.formData.password)) {
      errors.push('La clave de tarjeta debe ser exactamente 4 dígitos');
    }

    return errors;
  }

  onCloseModal(): void {
    this.showModal = false;
    this.cdr.markForCheck();
  }

  onViewAccounts(clientId: string): void {
    this.selectedClientId = clientId;
    this.showAccountsModal = true;
    
    this.clientService.obtener(clientId).subscribe({
      next: (client: any) => {
        this.currentClient = client;
        this.loadClientAccounts();
        this.selectedAccountId = '';
        this.accountMovements = [];
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error('Error loading client:', err);
        this.notificationService.show('Error al cargar datos del cliente', 'error');
      }
    });
  }

  onViewReports(clientId: string): void {
    this.selectedClientId = clientId;
    this.showReportsModal = true;
    this.cdr.markForCheck();
  }

  onCloseAccountsModal(): void {
    this.showAccountsModal = false;
    this.cdr.markForCheck();
  }

  onCloseReportsModal(): void {
    this.showReportsModal = false;
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

  // Métodos para la modal de cuentas y movimientos
  loadClientAccounts(): void {
    if (!this.selectedClientId) return;
    
    this.accountService.listarPorCliente(this.selectedClientId, 0, 100).subscribe({
      next: (response: any) => {
        this.clientAccounts = response.content || [];
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error('Error loading accounts:', err);
        this.notificationService.show('Error al cargar cuentas', 'error');
      }
    });
  }

  onSelectAccount(account: any): void {
    this.selectedAccountId = account.id;
    this.loadAccountMovements();
    this.cdr.markForCheck();
  }

  loadAccountMovements(): void {
    if (!this.selectedAccountId) return;
    
    this.movementService.listar(0, 50, this.selectedAccountId).subscribe({
      next: (response: any) => {
        this.accountMovements = response.content || [];
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error('Error loading movements:', err);
        this.notificationService.show('Error al cargar movimientos', 'error');
      }
    });
  }

  getSelectedAccountNumber(): string {
    const account = this.clientAccounts.find(a => a.id === this.selectedAccountId);
    return account && account.accountNumber ? account.accountNumber : 'Auto-generado';
  }

  onOpenAccountForm(mode: 'create' | 'edit', account?: any): void {
    this.showAccountFormModal = true;
    this.modalMode = mode;
    
    if (mode === 'create') {
      const newAccount = {
        accountType: 'AHORRO',
        initialBalance: 0,
        status: true,
        customerId: this.selectedClientId
      };
      this.accountFormData = newAccount;
    } else if (account) {
      this.accountFormData = { ...account };
    }
    
    this.cdr.markForCheck();
  }

  onEditAccount(account: any): void {
    this.onOpenAccountForm('edit', account);
  }

  onDeleteAccount(accountId: string): void {
    if (confirm('¿Estás seguro de que deseas eliminar esta cuenta?')) {
      this.accountService.eliminar(accountId).subscribe({
        next: () => {
          this.notificationService.show('Cuenta eliminada correctamente', 'success');
          this.loadClientAccounts();
        },
        error: () => {
          this.notificationService.show('Error al eliminar la cuenta', 'error');
        }
      });
    }
  }

  onSaveAccount(): void {
    if (!this.accountFormData.accountType) {
      this.notificationService.show('Debe completar todos los campos', 'warning');
      return;
    }

    if (this.modalMode === 'create') {
      this.accountService.crear(this.accountFormData).subscribe({
        next: () => {
          this.notificationService.show('Cuenta creada correctamente', 'success');
          this.showAccountFormModal = false;
          this.loadClientAccounts();
        },
        error: () => {
          this.notificationService.show('Error al crear la cuenta', 'error');
        }
      });
    } else {
      this.accountService.actualizarParcial(this.accountFormData.id, this.accountFormData).subscribe({
        next: () => {
          this.notificationService.show('Cuenta actualizada correctamente', 'success');
          this.showAccountFormModal = false;
          this.loadClientAccounts();
        },
        error: () => {
          this.notificationService.show('Error al actualizar la cuenta', 'error');
        }
      });
    }
  }

  onOpenMovementForm(mode: 'create' | 'edit', movement?: any): void {
    if (!this.selectedAccountId) {
      this.notificationService.show('Debe seleccionar una cuenta primero', 'warning');
      return;
    }
    this.showMovementFormModal = true;
    this.modalMode = mode;
    
    if (mode === 'create') {
      this.movementFormData = {
        description: '',
        amount: 0,
        transactionType: 'CREDITO',
        accountId: this.selectedAccountId
      };
    } else if (movement) {
      this.movementFormData = { ...movement };
    }
    
    this.cdr.markForCheck();
  }

  onSaveMovement(): void {
    if (!this.movementFormData.description || !this.movementFormData.amount || this.movementFormData.amount <= 0) {
      this.notificationService.show('Debe completar todos los campos correctamente', 'warning');
      return;
    }

    const movementData = {
      description: this.movementFormData.description,
      amount: this.movementFormData.amount,
      transactionType: this.movementFormData.transactionType,
      accountId: this.selectedAccountId
    };

    this.movementService.crear(movementData).subscribe({
      next: () => {
        this.notificationService.show('Movimiento registrado correctamente', 'success');
        this.showMovementFormModal = false;
        this.loadAccountMovements();
      },
      
    });
  }

  // Métodos para modal de confirmación
  onConfirmDelete(): void {
    if (this.confirmDeleteCallback) {
      this.confirmDeleteCallback();
    }
  }

  onCancelDelete(): void {
    this.showConfirmDeleteModal = false;
    this.confirmDeleteCallback = null;
    this.cdr.markForCheck();
  }
}

