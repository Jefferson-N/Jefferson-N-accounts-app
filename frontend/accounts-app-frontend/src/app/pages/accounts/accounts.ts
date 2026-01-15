import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, BehaviorSubject } from 'rxjs';
import { switchMap, shareReplay, tap, map } from 'rxjs/operators';
import { Account } from '../../services/account';
import { Client } from '../../services/client';
import { NotificationService } from '../../services/notification';
import { Cuenta, AccountRequest, Cliente } from '../../services/models';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './accounts.html',
  styleUrl: './accounts.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Accounts implements OnInit {
  searchTerm = '';
  accounts$!: Observable<Cuenta[]>;
  clients$!: Observable<Cliente[]>;
  allClients: Cliente[] = [];
  filteredClients: Cliente[] = [];
  clientFilterOpen = false;
  clientSearchTerm = '';
  pagination$ = new BehaviorSubject<{ currentPage: number; pageSize: number; totalPages: number; totalRecords: number }>({
    currentPage: 1,
    pageSize: 10,
    totalPages: 1,
    totalRecords: 0
  });

  showModal = false;
  modalMode: 'create' | 'edit' = 'create';
  selectedAccountId: string = '';

  formData: AccountRequest = {
    accountNumber: '',
    accountType: 'AHORRO',
    initialBalance: 0,
    status: true,
    customerId: ''
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
    private accountService: Account,
    private clientService: Client,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadClients();
    this.loadAccounts();
  }

  loadClients(): void {
    this.clientService.listar(0, 100, '').pipe(
      switchMap((response: any) => 
        new Promise<Cliente[]>(resolve => {
          this.allClients = response.content || [];
          this.filteredClients = this.allClients;
          resolve(this.allClients);
        })
      ),
      shareReplay(1)
    ).subscribe({
      next: (clients) => {
        this.clients$ = new Promise(resolve => resolve(clients)) as any;
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error('Error loading clients:', err);
        this.cdr.markForCheck();
      }
    });
  }

  loadAccounts(): void {
    this.accounts$ = this.pagination$.pipe(
      switchMap(pagination =>
        this.accountService.listar(pagination.currentPage - 1, pagination.pageSize, this.searchTerm).pipe(
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
    
    this.accounts$.subscribe({
      error: (err: any) => {
        console.error('Error loading accounts:', err);
        this.cdr.markForCheck();
      }
    });
  }

  onSearch(): void {
    this.pagination$.next({ ...this.pagination$.value, currentPage: 1 });
    this.loadAccounts();
  }

  onClientFilterToggle(): void {
    this.clientFilterOpen = !this.clientFilterOpen;
    if (this.clientFilterOpen) {
      this.filterClients();
    }
  }

  filterClients(): void {
    if (!this.clientSearchTerm.trim()) {
      this.filteredClients = [...this.allClients];
    } else {
      const term = this.clientSearchTerm.toLowerCase();
      this.filteredClients = this.allClients.filter(client =>
        client.name.toLowerCase().includes(term) ||
        client.identification.toLowerCase().includes(term)
      );
    }
  }

  onClientSelected(clientId: string): void {
    this.formData.customerId = clientId;
    this.clientFilterOpen = false;
    this.clientSearchTerm = '';
    this.cdr.markForCheck();
  }

  getSelectedClientName(): string {
    if (!this.formData.customerId) {
      return 'Seleccionar Cliente';
    }
    const client = this.allClients.find(c => c.id === this.formData.customerId);
    return client ? client.name : 'Seleccionar Cliente';
  }

  onNewAccount(): void {
    this.modalMode = 'create';
    this.formData = {
      accountNumber: '',
      accountType: 'AHORRO',
      initialBalance: 0,
      status: true,
      customerId: ''
    };
    this.showModal = true;
    this.cdr.markForCheck();
  }

  onEditAccount(id: string): void {
    this.accountService.obtener(id).subscribe({
      next: (account: Cuenta) => {
        this.modalMode = 'edit';
        this.selectedAccountId = id;
        this.formData = {
          accountNumber: account.accountNumber,
          accountType: account.accountType,
          initialBalance: account.initialBalance,
          status: account.status,
          customerId: account.customerId || ''
        };
        this.showModal = true;
        this.cdr.markForCheck();
      },
      error: () => {
        // El interceptor ya mostró la notificación de error
        this.cdr.markForCheck();
      }
    });
  }

  onDeleteAccount(id: string): void {
    if (confirm('¿Está seguro de que desea eliminar esta cuenta?')) {
      this.accountService.eliminar(id).subscribe({
        next: () => {
          this.notificationService.success('Cuenta eliminada correctamente');
          this.loadAccounts();
        },
        error: () => {
          // El interceptor ya mostró la notificación de error
          this.cdr.markForCheck();
        }
      });
    }
  }

  onSaveAccount(): void {
    const errors = this.validateAccountForm();
    if (errors.length > 0) {
      const errorMessage = errors.join('\n');
      this.notificationService.error(errorMessage, 6000);
      return;
    }

    if (this.modalMode === 'create') {
      this.accountService.crear(this.formData as Cuenta).subscribe({
        next: () => {
          this.notificationService.success('Cuenta creada correctamente');
          this.showModal = false;
          this.loadAccounts();
          this.cdr.markForCheck();
        },
        error: () => {
          // El interceptor ya mostró la notificación de error
          this.cdr.markForCheck();
        }
      });
    } else {
      this.accountService.actualizar(this.selectedAccountId, this.formData as Cuenta).subscribe({
        next: () => {
          this.notificationService.success('Cuenta actualizada correctamente');
          this.showModal = false;
          this.loadAccounts();
          this.cdr.markForCheck();
        },
        error: () => {
          // El interceptor ya mostró la notificación de error
          this.cdr.markForCheck();
        }
      });
    }
  }

  validateAccountForm(): string[] {
    const errors: string[] = [];

    if (!this.formData.accountNumber || this.formData.accountNumber.trim().length < 6) {
      errors.push('El número de cuenta debe tener al menos 6 caracteres');
    }
    if (this.formData.accountNumber && this.formData.accountNumber.length > 20) {
      errors.push('El número de cuenta no puede exceder 20 caracteres');
    }

    if (!this.formData.accountType) {
      errors.push('El tipo de cuenta es requerido');
    }

    if (this.formData.initialBalance === null || this.formData.initialBalance === undefined || this.formData.initialBalance < 0) {
      errors.push('El saldo inicial debe ser mayor o igual a 0');
    }

    if (!this.formData.customerId) {
      errors.push('El cliente es requerido');
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
      this.loadAccounts();
    }
  }

  onNextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.pagination$.next({ ...this.pagination$.value, currentPage: this.currentPage + 1 });
      this.loadAccounts();
    }
  }
}
