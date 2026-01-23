import { Component, Input, OnInit, Output, TemplateRef, ViewChild, inject, booleanAttribute } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import Swal from 'sweetalert2';

import { OtListDto, Page } from '../../model/ots';
import { OtService } from '../../service/ot.service';

// ¡OJO! NO uses EventEmitter de 'stream' → es de Node.js
import { EventEmitter } from '@angular/core';   // ← correcto
import { FormOtsComponent } from './form-ots-component/form-ots-component';
import { OtDetailComponent } from './ot-detail-component/ot-detail-component';

@Component({
  selector: 'app-ots',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    FormOtsComponent,
    OtDetailComponent
  ],
  templateUrl: './ots-component.html',
  styleUrl: './ots-component.css'
})
export class OtsComponent implements OnInit {
  private otService = inject(OtService);
  private modalService = inject(NgbModal);

  @Input() otId?: number | null;

  @Input({ transform: booleanAttribute })
  isViewMode = false;

  @Output() saved   = new EventEmitter<void>();
  @Output() canceled = new EventEmitter<void>();

  otsPage: Page<OtListDto> | null = null;
  dataSource: OtListDto[] = [];

  pageSize = 10;
  pageIndex = 0;
  totalElements = 0;
  totalPages = 0;

  searchText = '';
  loading = false;
  errorMessage: string | null = null;

  selectedOt: OtListDto | null = null;
  modalMode: 'create' | 'edit' | 'view' = 'create';

  @ViewChild('otModal') otModal!: TemplateRef<any>;

  ngOnInit(): void {
    this.loadOts();
  }

  loadOts(page: number = this.pageIndex): void {
    this.loading = true;
    this.errorMessage = null;

    this.otService
      .listarOts(
        this.searchText.trim() || undefined,
        page,
        this.pageSize,
        'ot,desc'
      )
      .subscribe({
        next: (pageData) => {
          this.otsPage = pageData;
          this.dataSource = pageData.content ?? [];
          this.totalElements = pageData.totalElements ?? 0;
          this.pageIndex = pageData.number ?? 0;
          this.pageSize = pageData.size ?? this.pageSize;
          this.totalPages = pageData.totalPages ?? 1;
          this.loading = false;
        },
        error: (err) => {
          this.errorMessage = err?.error?.message || 'Error al cargar las órdenes de trabajo';
          this.loading = false;
        },
      });
  }

  onSearchChange(): void {
    this.pageIndex = 0;
    this.loadOts();
  }

  // ──────────────────────────────────────────────
  //              PAGINACIÓN
  // ──────────────────────────────────────────────

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

  // ──────────────────────────────────────────────
  //              MODALES
  // ──────────────────────────────────────────────

  openCreateModal(): void {
    this.modalMode = 'create';
    this.selectedOt = null;
    this.openOtModal();
  }

  openEditModal(ot: OtListDto): void {
    this.modalMode = 'edit';
    this.selectedOt = { ...ot };
    this.openOtModal();
  }

  openViewModal(ot: OtListDto): void {
    this.modalMode = 'view';
    this.selectedOt = { ...ot };
    this.openOtModal();
  }

  private openOtModal(): void {
    const modalRef = this.modalService.open(this.otModal, {
      size: 'xl',
      backdrop: 'static',
      keyboard: true,
      centered: true,
      scrollable: true,
      windowClass: 'modal-xl-custom',
    });

    modalRef.result
      .then((result) => {
        if (result === 'saved') {
          this.loadOts();
          this.saved.emit();
        }
      })
      .catch(() => {
        this.canceled.emit();
      });
  }

 onFormSaved(): void {
  this.modalService.dismissAll('saved');  // Cierra TODOS los modales abiertos
  this.loadOts();                         // Recarga la lista automáticamente
}

onFormCanceled(): void {
  this.modalService.dismissAll('canceled'); // Cierra el modal al cancelar
}

  // ──────────────────────────────────────────────
  //           TOGGLE ESTADO (con Swal)
  // ──────────────────────────────────────────────

  toggleEstado(ot: OtListDto): void {
    const accion = ot.activo ? 'desactivar' : 'activar';
    const nuevoEstado = ot.activo ? 'inactiva' : 'activa';

    Swal.fire({
      title: `${accion.charAt(0).toUpperCase() + accion.slice(1)} esta OT?`,
      html: `La OT <strong>#${ot.ot}</strong> pasará a estado <strong>${nuevoEstado}</strong>`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: ot.activo ? '#dc3545' : '#28a745',
      cancelButtonColor: '#6c757d',
      confirmButtonText: `Sí, ${accion}`,
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.otService.toggleActivo(ot.idOts!).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: '¡Éxito!',
            text: `OT #${ot.ot} ahora está ${nuevoEstado}`,
            timer: 2200,
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
          });
          this.loadOts();
        },
        error: (err) => {
          Swal.fire('Error', err?.error?.message || 'No se pudo cambiar el estado', 'error');
        },
      });
    });
  }
}
