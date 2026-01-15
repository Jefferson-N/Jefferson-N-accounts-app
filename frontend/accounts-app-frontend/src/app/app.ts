import { Component, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NotificationComponent } from './shared/notification/notification.component';

@Component({
  selector: 'app-root',
  imports: [RouterModule, NotificationComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('accounts-app-frontend');
}
