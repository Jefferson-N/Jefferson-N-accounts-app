import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { NotificationService, Notification } from '../../services/notification';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="notification-container" *ngIf="notification$ | async as notification">
      <div [ngClass]="'notification notification--' + (notification?.type || 'info')">
        <i [ngClass]="getIcon(notification?.type || 'info')"></i>
        <span class="notification__message">{{ notification?.message }}</span>
        <button class="notification__close" (click)="close()">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .notification-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 2000;
      max-width: 400px;
    }

    .notification {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 20px;
      border-radius: 6px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      animation: slideIn 0.3s ease-out;
      font-size: 16px;
      font-weight: 500;

      &--success {
        background-color: #d4edda;
        color: #155724;
        border-left: 4px solid #28a745;
      }

      &--error {
        background-color: #f8d7da;
        color: #721c24;
        border-left: 4px solid #dc3545;
      }

      &--warning {
        background-color: #fff3cd;
        color: #856404;
        border-left: 4px solid #ffc107;
      }

      &--info {
        background-color: #d1ecf1;
        color: #0c5460;
        border-left: 4px solid #17a2b8;
      }

      i {
        font-size: 18px;
      }
    }

    .notification__message {
      flex: 1;
    }

    .notification__close {
      background: none;
      border: none;
      cursor: pointer;
      color: inherit;
      font-size: 16px;
      padding: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: opacity 0.2s;

      &:hover {
        opacity: 0.7;
      }
    }

    @keyframes slideIn {
      from {
        transform: translateX(400px);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    @media (max-width: 600px) {
      .notification-container {
        left: 10px;
        right: 10px;
        max-width: none;
      }
    }
  `]
})
export class NotificationComponent implements OnInit {
  notification$!: Observable<Notification | null>;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notification$ = this.notificationService.notification$;
  }

  close(): void {
    this.notificationService.clear();
  }

  getIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'success': 'fas fa-check-circle',
      'error': 'fas fa-exclamation-circle',
      'warning': 'fas fa-exclamation-triangle',
      'info': 'fas fa-info-circle'
    };
    return icons[type] || 'fas fa-bell';
  }
}
