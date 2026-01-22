import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

import { OtListDto, Page } from '../../model/ots';
import { OtService } from '../../service/ot.service';

@Component({
  selector: 'app-ots-component',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ots-component.html',
  styleUrl: './ots-component.css'
})
export class OtsComponent implements OnInit {
  private otService = inject(OtService);
  private router = inject(Router);

  otsPage: Page<OtListDto> | null = null;
  dataSource: OtListDto[] = [];

  pageSize = 10;
  pageIndex = 0;
  totalElements = 0;
  totalPages = 0;

  searchText: string = '';
  loading = false;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.loadOts();
  }

  loadOts(page: number = this.pageIndex): void {
    this.loading = true;
    this.errorMessage = null;

    this.otService.listarOts(
      this.searchText.trim() || undefined,
      page,
      this.pageSize,
      'ot,desc'
    ).subscribe({
      next: (pageData) => {
        this.otsPage = pageData;
        this.dataSource = pageData.content || [];
        this.totalElements = pageData.totalElements || 0;
        this.pageIndex = pageData.number || 0;
        this.pageSize = pageData.size || this.pageSize;
        this.totalPages = pageData.totalPages || 1;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Error al cargar las OTs';
        this.loading = false;
      }
    });
  }

  onSearchChange(): void {
    this.pageIndex = 0;
    this.loadOts();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.pageIndex) return;
    this.pageIndex = page;
    this.loadOts(page);
  }

  goToFirst(): void { this.goToPage(0); }
  goToLast(): void { this.goToPage(this.totalPages - 1); }
  goToPrevious(): void { if (this.pageIndex > 0) this.goToPage(this.pageIndex - 1); }
  goToNext(): void { if (this.pageIndex < this.totalPages - 1) this.goToPage(this.pageIndex + 1); }

  changePageSize(newSize: number): void {
    this.pageSize = newSize;
    this.pageIndex = 0;
    this.loadOts();
  }

  get visiblePages(): number[] {
    const range = 2;
    const start = Math.max(0, this.pageIndex - range);
    const end = Math.min(this.totalPages - 1, this.pageIndex + range);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  // Acciones
  goToCreate(): void {
    this.router.navigate(['/ot/nuevo']);
  }

  goToEdit(ot: OtListDto): void {
    this.router.navigate(['/ot/editar', ot.idOts]);
  }

  viewDetail(ot: OtListDto): void {
    this.router.navigate(['/ot', ot.idOts]);
  }

  toggleEstado(ot: OtListDto): void {
    const accion = ot.activo ? 'desactivar' : 'activar';
    const nuevoEstado = ot.activo ? 'inactiva' : 'activa';

    Swal.fire({
      title: `¿${accion.charAt(0).toUpperCase() + accion.slice(1)} esta OT?`,
      html: `La OT <strong>#${ot.ot}</strong> pasará a estado <strong>${nuevoEstado}</strong>`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: ot.activo ? '#dc3545' : '#28a745',
      cancelButtonColor: '#6c757d',
      confirmButtonText: `Sí, ${accion}`,
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.otService.toggleActivo(ot.idOts!).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: '¡Éxito!',
            text: `OT #${ot.ot} ahora está ${nuevoEstado}`,
            timer: 2500,
            toast: true,
            position: 'top-end',
            showConfirmButton: false
          });
          this.loadOts(); // recarga la lista
        },
        error: (err) => {
          Swal.fire('Error', err.message || 'No se pudo cambiar el estado', 'error');
        }
      });
    });
  }
}
