import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import Swal from 'sweetalert2';

import { OtService } from '../../../service/ot.service';
import { OtDetailResponse } from '../../../model/ots';

@Component({
  selector: 'app-ot-detail',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './ot-detail-component.html',
  styleUrls: ['./ot-detail-component.css'],
})
export class OtDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private otService = inject(OtService);

  @Input() otId?: number;
  @Input() otDetail?: OtDetailResponse; // Para uso directo desde modal

  ot: OtDetailResponse | null = null;
  loading = true;
  errorMessage = '';

  // Estados del flujo de trabajo
  estados: string[] = [
    'ASIGNACION',
    'PRESUPUESTO ENVIADO',
    'CREACION DE OC',
    'EN_EJECUCION',
    'EN_LIQUIDACION',
    'EN_FACTURACION',
    'FINALIZADO',
    'CANCELADA'
  ];

  ngOnInit(): void {
    // Si ya se pasó el detalle directamente (desde modal)
    if (this.otDetail) {
      this.ot = this.otDetail;
      this.loading = false;
      return;
    }

    // Obtener ID desde input o parámetro de ruta
    let id: number;

    if (this.otId !== undefined) {
      id = this.otId;
    } else {
      const idFromRoute = Number(this.route.snapshot.paramMap.get('id'));
      if (!idFromRoute || isNaN(idFromRoute)) {
        this.mostrarError('ID de OT inválido');
        return;
      }
      id = idFromRoute;
    }

    this.loadOt(id);
  }

  private loadOt(id: number): void {
    this.loading = true;
    this.otService.getOtById(id).subscribe({
      next: (detalle) => {
        this.ot = detalle;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'No se pudo cargar el detalle de la OT';
        this.mostrarError(this.errorMessage);
        this.loading = false;
      }
    });
  }

  // Timeline functions
  get currentEstadoIndex(): number {
    if (!this.ot?.estadoOt) return -1;
    const idx = this.estados.indexOf(this.ot.estadoOt);
    return idx >= 0 ? idx : -1;
  }

  getEstadoClass(index: number): string {
    if (index < this.currentEstadoIndex) return 'bg-success text-white';
    if (index === this.currentEstadoIndex) return 'bg-primary text-white shadow-lg';
    if (this.ot?.estadoOt === 'CANCELADA' && index === this.estados.length - 1) {
      return 'bg-danger text-white';
    }
    return 'bg-secondary text-white opacity-75';
  }

  getIconoEstado(index: number): string {
    const icons = [
      'bi-person-plus',           // ASIGNACION
      'bi-envelope-paper',        // PRESUPUESTO ENVIADO
      'bi-cart-check',            // CREACION DE OC
      'bi-gear-wide-connected',   // EN_EJECUCION
      'bi-calculator',            // EN_LIQUIDACION
      'bi-receipt-cutoff',        // EN_FACTURACION
      'bi-check2-circle',         // FINALIZADO
      'bi-x-octagon'              // CANCELADA
    ];
    return icons[index] || 'bi-question-circle';
  }

  getEstadoBadgeClass(estado?: string | null): string {
    if (!estado) return 'bg-secondary';

    switch (estado.toUpperCase()) {
      case 'FINALIZADO':
        return 'bg-success';
      case 'CANCELADA':
        return 'bg-danger';
      case 'EN_EJECUCION':
      case 'EN_LIQUIDACION':
      case 'EN_FACTURACION':
      case 'CREACION DE OC':
        return 'bg-warning text-dark';
      case 'PRESUPUESTO ENVIADO':
      case 'ASIGNACION':
        return 'bg-info';
      default:
        return 'bg-primary';
    }
  }

  private mostrarError(mensaje: string): void {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: mensaje,
      confirmButtonColor: '#dc3545',
      customClass: {
        container: 'swal2-container-modal'
      }
    }).then(() => this.volver());
  }

  volver(): void {
    // Cierra el modal si estamos en contexto modal
    const modalElement = document.querySelector('.modal.show');
    if (modalElement) {
      (modalElement as HTMLElement).click(); // Simula clic fuera para cerrar
    } else {
      this.router.navigate(['/ot']);
    }
  }

  editarOt(): void {
    if (this.ot?.idOts) {
      // Cerrar modal primero si estamos en uno
      const modalElement = document.querySelector('.modal.show');
      if (modalElement) {
        (modalElement as HTMLElement).click();
      }
      
      // Navegar a edición después de un breve delay
      setTimeout(() => {
        this.router.navigate(['/ot/edit', this.ot!.idOts]);
      }, 300);
    }
  }

  // Helper para calcular progreso
  get progressPercentage(): number {
    if (this.currentEstadoIndex < 0) return 0;
    return Math.round(((this.currentEstadoIndex + 1) / this.estados.length) * 100);
  }

  // Formatear fechas
  formatDate(dateString?: string | null): string {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatDateTime(dateString?: string | null): string {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}