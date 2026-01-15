import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Accounts } from './accounts';
import { Account } from '../../services/account';
import { Client } from '../../services/client';
import { NotificationService } from '../../services/notification';
import { of } from 'rxjs';
import { Cuenta, Cliente } from '../../services/models';

describe('Accounts Component', () => {
  let component: Accounts;
  let fixture: ComponentFixture<Accounts>;
  let accountService: jest.Mocked<Account>;
  let clientService: jest.Mocked<Client>;
  let notificationService: jest.Mocked<NotificationService>;

  const mockCuentas: Cuenta[] = [
    {
      id: '1',
      accountNumber: '001',
      accountType: 'AHORRO',
      initialBalance: 1000,
      currentBalance: 1500,
      status: true,
      customerId: 'cust-1'
    }
  ];

  const mockClientes: Cliente[] = [
    {
      id: 'cust-1',
      name: 'Juan Pérez',
      gender: 'MASCULINO',
      age: 30,
      identification: '123456789',
      address: 'Calle 1',
      phone: '123456789',
      password: 'pass',
      status: true
    }
  ];

  beforeEach(async () => {
    const accountServiceMock = {
      listar: jest.fn(),
      obtener: jest.fn(),
      crear: jest.fn(),
      actualizarParcial: jest.fn(),
      eliminar: jest.fn(),
      listarPorCliente: jest.fn()
    };
    const clientServiceMock = {
      listar: jest.fn()
    };
    const notificationServiceMock = {
      success: jest.fn(),
      error: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Accounts],
      providers: [
        { provide: Account, useValue: accountServiceMock },
        { provide: Client, useValue: clientServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock }
      ]
    }).compileComponents();

    accountService = TestBed.inject(Account) as jest.Mocked<Account>;
    clientService = TestBed.inject(Client) as jest.Mocked<Client>;
    notificationService = TestBed.inject(NotificationService) as jest.Mocked<NotificationService>;

    fixture = TestBed.createComponent(Accounts);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load clients when opening new account modal', () => {
    
    clientService.listar.mockReturnValue(
      of({ content: mockClientes, page: 0, size: 200, totalElements: 1, totalPages: 1 })
    );

    component.onNewAccount();

    
    expect(clientService.listar).toHaveBeenCalledWith(0, 200);
    expect(component.filteredClients.length).toBe(1);
    expect(component.filteredClients[0].name).toBe('Juan Pérez');
    expect(component.showModal).toBe(true);
    expect(component.modalMode).toBe('create');
  });

  it('should validate customer is required when creating account', () => {
    
    component.modalMode = 'create';
    component.formData = {
      accountType: 'AHORRO',
      initialBalance: 1000,
      status: true,
      customerId: ''
    };

    const errors = component.validateAccountForm();

    
    expect(errors).toContain('El cliente es requerido');
  });

  it('should not require customer when editing account', () => {
    
    component.modalMode = 'edit';
    component.formData = {
      accountType: 'AHORRO',
      initialBalance: 1000,
      status: true,
      customerId: ''
    };

    const errors = component.validateAccountForm();

    
    expect(errors).not.toContain('El cliente es requerido');
  });

  it('should load account data when editing', () => {
    
    accountService.obtener.mockReturnValue(of(mockCuentas[0]));

    component.onEditAccount('1');

    
    expect(accountService.obtener).toHaveBeenCalledWith('1');
    expect(component.modalMode).toBe('edit');
    expect(component.formData.accountType).toBe('AHORRO');
    expect(component.formData.currentBalance).toBe(1500);
    expect(component.showModal).toBe(true);
  });

  it('should update account without sending currentBalance', () => {
    component.modalMode = 'edit';
    component.selectedAccountId = '1';
    component.formData = {
      accountType: 'CORRIENTE',
      initialBalance: 1000,
      status: false,
      customerId: 'cust-1',
      currentBalance: 2000
    };
    accountService.actualizarParcial.mockReturnValue(of(mockCuentas[0]));
    accountService.listar.mockReturnValue(
      of({ content: mockCuentas, page: 0, size: 10, totalElements: 1, totalPages: 1 })
    );

    component.onSaveAccount();
    const callArgs = accountService.actualizarParcial.mock.calls[0];
    expect(callArgs[1]).toEqual({
      accountType: 'CORRIENTE',
      status: false
    });
    expect(callArgs[1]).not.toHaveProperty('currentBalance');
    expect(notificationService.success).toHaveBeenCalledWith('Cuenta actualizada correctamente');
  });
});
