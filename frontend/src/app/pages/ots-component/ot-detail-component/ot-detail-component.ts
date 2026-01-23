import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import Swal from 'sweetalert2';

import { OtService } from '../../../service/ot.service';
import { OtDetailResponse } from '../../../model/ots';  // ← Asegúrate de usar este

@Component({
  selector: 'app-ot-detail',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './ot-detail-component.html',
  styleUrl: './ot-detail-component.css',
})
export class OtDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private otService = inject(OtService);
@Input() otId?: number;
  ot: OtDetailResponse | null = null;
  loading = true;
  errorMessage = '';

 ngOnInit(): void {
  let id: number;

  // Prioridad: input > ruta
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
      this.errorMessage = err?.message || 'No se pudo cargar el detalle';
      this.mostrarError(this.errorMessage);
      this.loading = false;
    }
  });
}
// En la clase OtDetailComponent

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
    'bi bi-person-plus',           // ASIGNACION
    'bi bi-envelope-paper',        // PRESUPUESTO ENVIADO
    'bi bi-cart-check',            // CREACION DE OC
    'bi bi-gear-wide-connected',   // EN_EJECUCION
    'bi bi-calculator',            // EN_LIQUIDACION
    'bi bi-receipt-cutoff',        // EN_FACTURACION
    'bi bi-check2-circle',         // FINALIZADO
    'bi bi-x-octagon'              // CANCELADA
  ];
  return icons[index] || 'bi bi-question-circle';
}

getEstadoBadgeClass(estado?: string | null): string {
  if (!estado) return 'bg-secondary';

  if (estado === 'FINALIZADO') return 'bg-success';
  if (estado === 'CANCELADA') return 'bg-danger';
  if (estado.includes('EN_') || estado === 'CREACION DE OC') return 'bg-warning text-dark';
  if (estado === 'PRESUPUESTO ENVIADO' || estado === 'ASIGNACION') return 'bg-info';

  return 'bg-primary';
}
  private mostrarError(mensaje: string): void {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: mensaje,
      confirmButtonColor: '#dc3545',
    }).then(() => this.volver());
  }

  volver(): void {
    this.router.navigate(['/ot']);
  }

  editarOt(): void {
    if (this.ot?.idOts) {
      this.router.navigate(['/ot/edit', this.ot.idOts]);
    }
  }
}
