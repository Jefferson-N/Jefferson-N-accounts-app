import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Report } from '../../services/report';
import { Client } from '../../services/client';
import { NotificationService } from '../../services/notification';
import { Cliente } from '../../services/models';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.html',
  styleUrl: './reports.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Reports implements OnInit {
  clients: Cliente[] = [];
  filteredClients: Cliente[] = [];
  selectedClientId: string = '';
  clientFilterOpen = false;
  clientSearchTerm = '';
  dateFrom: string = '';
  dateTo: string = '';
  reportFormat: 'json' | 'pdf' = 'json';
  isLoading = false;
  reportGenerated = false;
  reportData: any = null;

  constructor(
    private reportService: Report,
    private clientService: Client,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadClients();
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.dateFrom = firstDay.toISOString().split('T')[0];
    this.dateTo = today.toISOString().split('T')[0];
  }

  loadClients(): void {
    this.clientService.listar(0, 200, '').subscribe({
      next: (response: any) => {
        this.clients = response.content || [];
        this.filteredClients = this.clients;
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error('Error loading clients:', err);
        this.notificationService.error('Error al cargar clientes');
      }
    });
  }

  onClientFilterToggle(): void {
    this.clientFilterOpen = !this.clientFilterOpen;
    if (this.clientFilterOpen) {
      this.filterClients();
    }
  }

  filterClients(): void {
    if (!this.clientSearchTerm.trim()) {
      this.filteredClients = [...this.clients];
    } else {
      const term = this.clientSearchTerm.toLowerCase();
      this.filteredClients = this.clients.filter(client =>
        client.name.toLowerCase().includes(term) ||
        client.identification.toLowerCase().includes(term)
      );
    }
  }

  onClientSelected(clientId: string): void {
    this.selectedClientId = clientId;
    this.clientFilterOpen = false;
    this.clientSearchTerm = '';
    this.cdr.markForCheck();
  }

  getSelectedClientName(): string {
    if (!this.selectedClientId) {
      return 'Seleccionar Cliente';
    }
    const client = this.clients.find(c => c.id === this.selectedClientId);
    return client ? client.name : 'Seleccionar Cliente';
  }

  onClientSelect(): void {
    this.cdr.markForCheck();
  }

  onGenerateReport(): void {
    // Validaciones
    const errors = this.validateReportForm();
    if (errors.length > 0) {
      errors.forEach(error => this.notificationService.error(error));
      return;
    }

    this.isLoading = true;
    this.cdr.markForCheck();
    
    this.reportService.generarReporte(this.selectedClientId, this.dateFrom, this.dateTo).subscribe({
      next: (response: any) => {
        this.reportData = response;
        this.reportGenerated = true;
        this.isLoading = false;
        this.notificationService.success('Reporte generado correctamente');
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        // El interceptor ya mostró la notificación de error
        this.cdr.markForCheck();
      }
    });
  }

  onDownload(): void {
    if (this.reportFormat === 'json') {
      this.onDownloadJSON();
    } else {
      this.onDownloadPDF();
    }
  }

  onDownloadJSON(): void {
    if (!this.reportData) {
      this.notificationService.warning('Debe generar un reporte primero');
      return;
    }

    const dataStr = JSON.stringify(this.reportData, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    const clientName = this.getClientName(this.selectedClientId);
    link.download = `reporte_${clientName}_${new Date().toISOString().split('T')[0]}.json`;
    link.click();
    URL.revokeObjectURL(url);
    this.notificationService.success('Reporte JSON descargado');
  }

  onDownloadPDF(): void {
    if (!this.selectedClientId) {
      this.notificationService.warning('Debe seleccionar un cliente');
      return;
    }

    this.reportService.descargarPDF(this.selectedClientId, this.dateFrom, this.dateTo).subscribe({
      next: (response: Blob) => {
        const url = URL.createObjectURL(response);
        const link = document.createElement('a');
        link.href = url;
        const clientName = this.getClientName(this.selectedClientId);
        link.download = `reporte_${clientName}_${new Date().toISOString().split('T')[0]}.pdf`;
        link.click();
        URL.revokeObjectURL(url);
        this.notificationService.success('Reporte PDF descargado');
      },
      error: () => {
        // El interceptor ya mostró la notificación de error
      }
    });
  }

  getClientName(clientId: string): string {
    const client = this.clients.find(c => c.id === clientId);
    return client ? client.name : 'Cliente';
  }

  clearReport(): void {
    this.reportGenerated = false;
    this.reportData = null;
    this.cdr.markForCheck();
  }

  validateReportForm(): string[] {
    const errors: string[] = [];

    if (!this.selectedClientId) {
      errors.push('Debe seleccionar un cliente');
    }

    if (!this.dateFrom) {
      errors.push('La fecha inicial es requerida');
    }

    if (!this.dateTo) {
      errors.push('La fecha final es requerida');
    }

    if (this.dateFrom && this.dateTo && new Date(this.dateFrom) > new Date(this.dateTo)) {
      errors.push('La fecha inicial no puede ser mayor a la fecha final');
    }

    return errors;
  }
}
