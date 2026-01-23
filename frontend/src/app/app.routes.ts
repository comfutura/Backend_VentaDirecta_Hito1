// src/app/app.routes.ts
import { Routes } from '@angular/router';

import { LoginComponent } from './pages/login-componente/login-componente';
import { DashboardComponent } from './pages/dashboard-componente/dashboard-componente';
import { OtsComponent } from './pages/ots-component/ots-component';
import { LayoutComponent } from './component/layaout-component/layaout-component';
import { SiteComponent } from './pages/site-component/site-component';
import { OrdenCompraComponent } from './pages/orden-compra-component/orden-compra-component';

import { authGuard } from './auth/auth.guard';
import { GestionCargosSolicitantesComponent } from './pages/gestion-cargos-solicitantes-component/gestion-cargos-solicitantes-component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: '',
    component: LayoutComponent,
    canActivateChild: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },

      { path: 'ot', component: OtsComponent },
      { path: 'site', component: SiteComponent },
      { path: 'gestion-jefatura-analista', component: GestionCargosSolicitantesComponent },
      { path: 'orden-compra', component: OrdenCompraComponent },
    ],
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
