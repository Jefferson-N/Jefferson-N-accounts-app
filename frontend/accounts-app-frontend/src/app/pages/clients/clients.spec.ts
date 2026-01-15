import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Clients } from './clients';
import { Client } from '../../services/client';
import { NotificationService } from '../../services/notification';
import { of } from 'rxjs';
import { Cliente } from '../../services/models';

describe('Clients Component', () => {
  let component: Clients;
  let fixture: ComponentFixture<Clients>;
  let clientService: jest.Mocked<Client>;
  let notificationService: jest.Mocked<NotificationService>;

  const mockClientes: Cliente[] = [
    {
      id: '1',
      name: 'Juan Pérez',
      gender: 'MASCULINO',
      age: 30,
      identification: '123456789',
      address: 'Calle 1',
      phone: '0987654321',
      password: 'password',
      status: true
    },
    {
      id: '2',
      name: 'María García',
      gender: 'FEMENINO',
      age: 28,
      identification: '987654321',
      address: 'Calle 2',
      phone: '0987654322',
      password: 'password',
      status: true
    }
  ];

  beforeEach(async () => {
    const clientServiceMock = {
      listar: jest.fn(),
      obtener: jest.fn(),
      crear: jest.fn(),
      actualizar: jest.fn(),
      eliminar: jest.fn()
    };
    const notificationServiceMock = {
      success: jest.fn(),
      error: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Clients],
      providers: [
        { provide: Client, useValue: clientServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock }
      ]
    }).compileComponents();

    clientService = TestBed.inject(Client) as jest.Mocked<Client>;
    notificationService = TestBed.inject(NotificationService) as jest.Mocked<NotificationService>;

    fixture = TestBed.createComponent(Clients);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load clients on init', () => {
    clientService.listar.mockReturnValue(
      of({ content: mockClientes, page: 0, size: 10, totalElements: 2, totalPages: 1 })
    );

    component.ngOnInit();

    expect(clientService.listar).toHaveBeenCalledWith(0, 10, '');
  });

  it('should open create modal with empty form data', () => {
    component.onNewClient();

    expect(component.showModal).toBe(true);
    expect(component.modalMode).toBe('create');
    expect(component.formData.name).toBe('');
    expect(component.formData.gender).toBe('');
  });

  it('should open delete confirmation modal with correct message', () => {
    component.onDeleteClient('1');

    expect(component.showConfirmDeleteModal).toBe(true);
    expect(component.confirmDeleteMessage).toContain('¿Está seguro de que desea eliminar este cliente?');
  });

  it('should validate required fields when creating client', () => {
    component.modalMode = 'create';
    component.formData = {
      name: '',
      gender: '',
      age: 0,
      identification: '',
      address: '',
      phone: '',
      password: '',
      status: true
    };

    const errors = component.validateClientForm();

    expect(errors.length).toBeGreaterThan(0);
    expect(errors).toContain('El nombre debe tener al menos 8 caracteres');
  });

  it('should cancel delete operation and close modal', () => {
    component.showConfirmDeleteModal = true;

    component.onCancelDelete();

    expect(component.showConfirmDeleteModal).toBe(false);
    expect(component.confirmDeleteCallback).toBeNull();
  });
});
