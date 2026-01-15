import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, BehaviorSubject } from 'rxjs';
import { switchMap, shareReplay, tap, map } from 'rxjs/operators';
import { Account } from '../../services/account';
import { Client } from '../../services/client';
import { NotificationService } from '../../services/notification';
import { Cuenta, AccountRequest, Cliente } from '../../services/models';
import { Movements } from '../movements/movements';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, FormsModule, Movements],
  templateUrl: './accounts.html',
  styleUrl: './accounts.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Accounts implements OnInit {
  @Input() clientIdFilter: string = '';
  @Input() isModal: boolean = false;
  
  searchTerm = '';
  accounts$!: Observable<Cuenta[]>;
  pagination$ = new BehaviorSubject<{ currentPage: number; pageSize: number; totalPages: number; totalRecords: number }>({
    currentPage: 1,
    pageSize: 10,
    totalPages: 1,
    totalRecords: 0
  });

  showModal = false;
  modalMode: 'create' | 'edit' = 'create';
  selectedAccountId: string = '';
  selectedAccount: Cuenta | null = null;
  
  // Modal de movimientos
  showMovementsModal = false;

  // Modal de confirmación
  showConfirmDeleteModal = false;
  confirmDeleteMessage = '';
  confirmDeleteCallback: (() => void) | null = null;

  formData: AccountRequest & { currentBalance?: number } = {
    accountType: 'AHORRO',
    initialBalance: 0,
    status: true,
    customerId: '',
    currentBalance: 0
  };

  filteredClients: Cliente[] = [];

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
    if (this.clientIdFilter) {
      this.formData.customerId = this.clientIdFilter;
      this.loadAccountsByClient(this.clientIdFilter);
    } else {
      this.loadAccounts();
    }
  }

  loadAccountsByClient(clientId: string): void {
    this.accountService.listarPorCliente(clientId).subscribe({
      next: (response: any) => {
        this.accounts$ = new Promise(resolve => resolve(response.content || response)) as any;
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error('Error loading accounts:', err);
        this.cdr.markForCheck();
      }
    });
  }

  loadClients(): void {
    this.clientService.listar(0, 200).subscribe({
      next: (response: any) => {
        this.filteredClients = response.content || response || [];
        this.cdr.markForCheck();
      },
      error: () => this.cdr.markForCheck()
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

  onNewAccount(): void {
    this.modalMode = 'create';
    this.selectedAccount = null;
    this.formData = {
      accountType: 'AHORRO',
      initialBalance: 0,
      status: true,
      customerId: '',
      currentBalance: 0
    };
    this.loadClients();
    this.showModal = true;
    this.cdr.markForCheck();
  }

  onEditAccount(id: string): void {
    this.accountService.obtener(id).subscribe({
      next: (account: Cuenta) => {
        this.modalMode = 'edit';
        this.selectedAccountId = id;
        this.selectedAccount = account; 
        this.formData = {
          accountType: account.accountType,
          initialBalance: account.initialBalance,
          status: account.status,
          customerId: account.customerId || '',
          currentBalance: account.currentBalance || 0
        };
        this.showModal = true;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.markForCheck();
      }
    });
  }

  onDeleteAccount(id: string): void {
    this.confirmDeleteMessage = '¿Está seguro de que desea eliminar esta cuenta?';
    this.confirmDeleteCallback = () => {
      this.accountService.eliminar(id).subscribe({
        next: () => {
          this.notificationService.success('Cuenta eliminada correctamente');
          this.showConfirmDeleteModal = false;
          this.loadAccounts();
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
          this.cdr.markForCheck();
        }
      });
    } else {
      // En edición, solo enviar los campos que se pueden actualizar
      const updateData = {
        accountType: this.formData.accountType,
        status: this.formData.status
      };
      this.accountService.actualizarParcial(this.selectedAccountId, updateData).subscribe({
        next: () => {
          this.notificationService.success('Cuenta actualizada correctamente');
          this.showModal = false;
          this.loadAccounts();
          this.cdr.markForCheck();
        },
        error: () => {
          this.cdr.markForCheck();
        }
      });
    }
  }

  validateAccountForm(): string[] {
    const errors: string[] = [];

    if (!this.formData.accountType) {
      errors.push('El tipo de cuenta es requerido');
    }

    if (this.formData.initialBalance === null || this.formData.initialBalance === undefined || this.formData.initialBalance < 0) {
      errors.push('El saldo inicial debe ser mayor o igual a 0');
    }

    if (this.modalMode === 'create' && !this.formData.customerId) {
      errors.push('El cliente es requerido');
    }

    return errors;
  }

  onCloseModal(): void {
    this.showModal = false;
    this.cdr.markForCheck();
  }

  onViewMovements(accountId: string): void {
    this.accountService.obtener(accountId).subscribe({
      next: (account: Cuenta) => {
        this.selectedAccount = account;
        this.selectedAccountId = accountId;
        this.showMovementsModal = true;
        this.cdr.markForCheck();
      },
      error: () => {
        this.cdr.markForCheck();
      }
    });
  }

  onCloseMovementsModal(): void {
    this.showMovementsModal = false;
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
