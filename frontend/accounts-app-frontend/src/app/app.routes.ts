import { Routes } from '@angular/router';
import { Clients } from './pages/clients/clients';
import { Accounts } from './pages/accounts/accounts';
import { Movements } from './pages/movements/movements';
import { Reports } from './pages/reports/reports';

export const routes: Routes = [
  { path: '', redirectTo: 'clients', pathMatch: 'full' },
  { path: 'clients', component: Clients },
  { path: 'accounts', component: Accounts },
  { path: 'movements', component: Movements },
  { path: 'reports', component: Reports }
];
