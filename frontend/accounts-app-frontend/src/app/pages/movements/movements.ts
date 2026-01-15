import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, BehaviorSubject } from 'rxjs';
import { switchMap, shareReplay, tap, map } from 'rxjs/operators';
import { Movement } from '../../services/movement';
import { Account } from '../../services/account';
import { NotificationService } from '../../services/notification';
import { Movimiento, TransactionRequest, Cuenta } from '../../services/models';

@Component({
  selector: 'app-movements',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './movements.html',
  styleUrl: './movements.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Movements implements OnInit {
  @Input() accountIdFilter: string = '';
  @Input() isModal: boolean = false;
  
  movements$!: Observable<Movimiento[]>;
  accounts$!: Observable<Cuenta[]>;
  allAccounts: Cuenta[] = [];
  filteredAccounts: Cuenta[] = [];
  selectedAccount: Cuenta | null = null;
  selectedAccountId = '';
  searchTerm = '';
  accountFilterOpen = false;
  accountSearchTerm = '';
  pagination$ = new BehaviorSubject<{ currentPage: number; pageSize: number; totalPages: number; totalRecords: number }>({
    currentPage: 1,
    pageSize: 10,
    totalPages: 1,
    totalRecords: 0
  });

  showModal = false;
  modalMode: 'create' | 'edit' = 'create';
  selectedMovementId: string = '';

  formData: TransactionRequest = {
    date: new Date().toISOString().split('T')[0],
    description: '',
    transactionType: 'CREDITO',
    amount: 0,
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
    private movementService: Movement,
    private accountService: Account,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAccounts();
    if (this.accountIdFilter) {
      this.selectedAccountId = this.accountIdFilter;
      this.formData.accountId = this.accountIdFilter;
      // Cargar la cuenta para mostrar el saldo
      this.accountService.obtener(this.accountIdFilter).subscribe({
        next: (account: Cuenta) => {
          this.selectedAccount = account;
          this.cdr.markForCheck();
        },
        error: () => {
          this.cdr.markForCheck();
        }
      });
      this.loadMovementsByAccount(this.accountIdFilter);
    } else {
      this.loadMovements();
    }
  }

  loadAccounts(): void {
    this.accountService.listar(0, 100, '').subscribe({
      next: (response: any) => {
        this.allAccounts = response.content || [];
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error('Error loading accounts:', err);
      }
    });
  }

  loadMovementsByAccount(accountId: string): void {
    this.movementService.listarPorCuenta(accountId).subscribe({
      next: (response: any) => {
        this.movements$ = new Promise(resolve => resolve(response.content || response)) as any;
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error('Error loading movements:', err);
        this.cdr.markForCheck();
      }
    });
  }

  loadMovements(): void {
    this.movements$ = this.pagination$.pipe(
      switchMap(pagination =>
        this.movementService.listar(pagination.currentPage - 1, pagination.pageSize, this.selectedAccountId).pipe(
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
    
    this.movements$.subscribe({
      error: (err: any) => {
        console.error('Error loading movements:', err);
        this.cdr.markForCheck();
      }
    });
  }

  onSearch(): void {
    this.pagination$.next({ ...this.pagination$.value, currentPage: 1 });
    this.loadMovements();
  }

  onAccountFilterToggle(): void {
    this.accountFilterOpen = !this.accountFilterOpen;
    if (this.accountFilterOpen) {
      this.filterAccounts();
    }
  }

  filterAccounts(): void {
    if (!this.accountSearchTerm.trim()) {
      this.filteredAccounts = [...this.allAccounts];
    } else {
      const term = this.accountSearchTerm.toLowerCase();
      this.filteredAccounts = this.allAccounts.filter(account =>
        account.accountNumber.toLowerCase().includes(term) ||
        account.accountType.toLowerCase().includes(term)
      );
    }
  }

  onAccountSelected(accountId: string): void {
    this.selectedAccountId = accountId;
    this.accountFilterOpen = false;
    this.accountSearchTerm = '';
    this.onSearch();
  }

  getSelectedAccountName(): string {
    if (!this.formData.accountId) {
      return 'Seleccionar Cuenta';
    }
    const account = this.allAccounts.find(a => a.id === this.formData.accountId);
    return account ? `${account.accountNumber} - ${account.accountType}` : 'Cuenta no encontrada';
  }

  onNewMovement(): void {
    this.modalMode = 'create';
    this.formData = {
      date: new Date().toISOString().split('T')[0],
      description: '',
      transactionType: 'CREDITO',
      amount: 0,
      accountId: this.selectedAccountId
    };
    this.showModal = true;
    this.cdr.markForCheck();
  }

  onSaveMovement(): void {
    // Validaciones
    const errors = this.validateMovementForm();
    if (errors.length > 0) {
      const errorMessage = errors.join('\n');
      this.notificationService.error(errorMessage, 6000);
      return;
    }

    this.movementService.crear(this.formData).subscribe({
      next: () => {
        this.notificationService.success('Movimiento creado correctamente');
        this.showModal = false;
        this.formData = {
          date: new Date().toISOString().split('T')[0],
          description: '',
          transactionType: 'CREDITO',
          amount: 0,
          accountId: ''
        };
        this.loadMovements();
        this.cdr.markForCheck();
      },
      error: () => {
        this.cdr.markForCheck();
      }
    });
  }

  validateMovementForm(): string[] {
    const errors: string[] = [];

    // Descripción: requerida, mín 3 caracteres, máx 200
    if (!this.formData.description || this.formData.description.trim().length < 3) {
      errors.push('La descripción debe tener al menos 3 caracteres');
    }
    if (this.formData.description && this.formData.description.length > 200) {
      errors.push('La descripción no puede exceder 200 caracteres');
    }

    // Tipo: requerido
    if (!this.formData.transactionType) {
      errors.push('El tipo de movimiento es requerido');
    }

    // Monto: mayor a 0
    if (!this.formData.amount || this.formData.amount <= 0) {
      errors.push('El monto debe ser mayor a 0');
    }

    // Cuenta: requerida
    if (!this.formData.accountId) {
      errors.push('La cuenta es requerida');
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
      this.loadMovements();
    }
  }

  onNextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.pagination$.next({ ...this.pagination$.value, currentPage: this.currentPage + 1 });
      this.loadMovements();
    }
  }
}
