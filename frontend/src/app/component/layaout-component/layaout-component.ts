import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './layaout-component.html',
  styleUrl: './layaout-component.css',
})
export class LayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private sanitizer = inject(DomSanitizer);

  isCollapsed = false;
  isMobileOpen = false;
  logoUrl: SafeUrl | null = null;

  ngOnInit() {
    this.loadLogo();
  }

  private loadLogo() {
    try {
      const logoPath = 'src/app/imgs/COMFUTURA LOGOTIPO-02.png';
      this.logoUrl = this.sanitizer.bypassSecurityTrustUrl(logoPath);
    } catch (error) {
      console.warn('Logo no encontrado, usando ícono por defecto');
    }
  }

  get username(): string {
    return this.authService.currentUser?.username ?? 'Usuario';
  }

  get userInitial(): string {
    const name = this.authService.currentUser?.username;
    return name ? name.charAt(0).toUpperCase() : 'U';
  }

  get currentUser() {
    return this.authService.currentUser;
  }

  get mainRole(): string {
    const roles = this.currentUser?.roles;
    if (!roles || roles.length === 0) return '';
    // Priorizar ADMIN si existe
    const adminRole = roles.find(r => r.includes('ADMIN'));
    return adminRole || roles[0];
  }

  get userBadgeClass(): string {
    const role = this.mainRole.toLowerCase();
    if (role.includes('admin')) return 'admin-badge';
    if (role.includes('gerente') || role.includes('jefe')) return 'manager-badge';
    return 'user-badge';
  }

  toggleCollapse() {
    this.isCollapsed = !this.isCollapsed;
  }

  toggleMobile() {
    this.isMobileOpen = !this.isMobileOpen;
    if (this.isMobileOpen) {
      document.body.classList.add('no-scroll');
    } else {
      document.body.classList.remove('no-scroll');
    }
  }

  closeMobile() {
    this.isMobileOpen = false;
    document.body.classList.remove('no-scroll');
  }

  logout() {
    Swal.fire({
      title: '¿Cerrar sesión?',
      text: 'Estás a punto de salir del sistema',
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, cerrar sesión',
      cancelButtonText: 'Cancelar',
      reverseButtons: true
    }).then((result) => {
      if (result.isConfirmed) {
        this.authService.logout();
        this.router.navigate(['/login']);

        Swal.fire({
          icon: 'success',
          title: 'Sesión cerrada',
          text: 'Has salido del sistema correctamente',
          timer: 1500,
          showConfirmButton: false
        });
      }
    });
  }

  // Método para verificar si hay notificaciones
  get hasNotifications(): boolean {
    return false; // Implementa lógica real si tienes notificaciones
  }
}
