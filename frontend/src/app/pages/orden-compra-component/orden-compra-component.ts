// orden-compra-component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { OrdenCompraService } from '../../service/orden-compra.service'; // ajusta ruta
import { OrdenCompraResponse, PageOrdenCompra } from '../../model/orden-compra.model';
import { FormOrdenCompraComponent } from "./form-orden-compra-component/form-orden-compra-component"; // ajusta ruta

@Component({
  selector: 'app-orden-compra-component',
  standalone: true,
  imports: [CommonModule, FormsModule, FormOrdenCompraComponent],
  templateUrl: './orden-compra-component.html',
  styleUrls: ['./orden-compra-component.css']
})
export class OrdenCompraComponent implements OnInit {

  pageData: PageOrdenCompra | null = null;
  ordenes: OrdenCompraResponse[] = [];
  filteredOrdenes: OrdenCompraResponse[] = [];

  searchTerm: string = '';
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 1;

  isLoading: boolean = false;
  errorMessage: string | null = null;

  // Para el formulario modal
  showFormModal: boolean = false;
  isEditMode: boolean = false;
  selectedOc: OrdenCompraResponse | null = null;

  constructor(private ordenService: OrdenCompraService) {}

  ngOnInit(): void {
    this.cargarOrdenes();
  }

  cargarOrdenes(page: number = 0): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.ordenService.listar(page, this.pageSize).subscribe({
      next: (data) => {
        this.pageData = data;
        this.ordenes = data.content;
        this.filteredOrdenes = [...this.ordenes];
        this.currentPage = data.number;
        this.totalPages = data.totalPages;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'No se pudieron cargar las órdenes';
        this.mostrarError(this.errorMessage);
        this.isLoading = false;
      }
    });
  }

  aplicarFiltro(): void {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      this.filteredOrdenes = [...this.ordenes];
      return;
    }

    this.filteredOrdenes = this.ordenes.filter(oc =>
      oc.otsNombre.toLowerCase().includes(term) ||
      oc.proveedorNombre.toLowerCase().includes(term) ||
      oc.maestroCodigo.toLowerCase().includes(term) ||
      oc.estadoOcNombre.toLowerCase().includes(term) ||
      (oc.observacion?.toLowerCase().includes(term) ?? false)
    );
  }

  onSearchChange(): void {
    this.aplicarFiltro();
  }

  irAPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPages) {
      this.cargarOrdenes(pagina);
    }
  }

  // ──────────────────────────────────────────────
  //            Acciones de la tabla
  // ──────────────────────────────────────────────

  verDetalle(oc: OrdenCompraResponse): void {
    Swal.fire({
      title: `Orden #${oc.idOc}`,
      html: `
        <div class="text-start">
          <p><strong>OT:</strong> ${oc.otsNombre}</p>
          <p><strong>Material:</strong> ${oc.maestroCodigo}</p>
          <p><strong>Proveedor:</strong> ${oc.proveedorNombre}</p>
          <p><strong>Estado:</strong> ${oc.estadoOcNombre}</p>
          <p><strong>Cantidad:</strong> ${oc.cantidad}</p>
          <p><strong>Costo unit.:</strong> ${oc.costoUnitario.toLocaleString('es-PE', { style: 'currency', currency: 'PEN' })}</p>
          <p><strong>Total:</strong> ${(oc.cantidad * oc.costoUnitario).toLocaleString('es-PE', { style: 'currency', currency: 'PEN' })}</p>
          <p><strong>Fecha:</strong> ${new Date(oc.fechaOc).toLocaleString('es-PE')}</p>
          ${oc.observacion ? `<p><strong>Observación:</strong> ${oc.observacion}</p>` : ''}
        </div>
      `,
      icon: 'info',
      confirmButtonText: 'Cerrar'
    });
  }

  editarOrden(oc: OrdenCompraResponse): void {
    this.selectedOc = { ...oc }; // copia para editar
    this.isEditMode = true;
    this.showFormModal = true;
  }

  toggleEstado(oc: OrdenCompraResponse): void {
    const nuevosEstadosPosibles = {
      'PENDIENTE': 'EN PROCESO',
      'EN PROCESO': 'APROBADA',
      'APROBADA': 'ATENDIDA',
      'ATENDIDA': 'CERRADA',
      'RECHAZADA': 'PENDIENTE',
      'ANULADA': 'PENDIENTE'
    };

    const nuevoEstadoNombre = nuevosEstadosPosibles[oc.estadoOcNombre as keyof typeof nuevosEstadosPosibles] || 'PENDIENTE';

    Swal.fire({
      title: '¿Cambiar estado?',
      text: `Pasar de "${oc.estadoOcNombre}" → "${nuevoEstadoNombre}"?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, cambiar'
    }).then((result) => {
      if (result.isConfirmed) {
        // Aquí deberías tener un endpoint o lógica para cambiar solo estado
        // Por simplicidad, suponemos que actualizamos todo el objeto (o crea método específico)
        const request = {
          estadoOcId: this.mapEstadoNombreToId(nuevoEstadoNombre), // necesitas implementar este mapeo
          otsId: oc.idOc, // ← ojo: probablemente no es correcto, ajusta según modelo real
          maestroId: 0,   // ← faltan datos reales → idealmente recargar o tenerlos
          proveedorId: 0,
          cantidad: oc.cantidad,
          costoUnitario: oc.costoUnitario,
          observacion: oc.observacion
        };

        this.ordenService.actualizar(oc.idOc, request).subscribe({
          next: () => {
            this.mostrarExito(`Estado cambiado a ${nuevoEstadoNombre}`);
            this.cargarOrdenes(this.currentPage);
          },
          error: () => this.mostrarError('No se pudo cambiar el estado')
        });
      }
    });
  }

  // Helper – ajusta según tus IDs reales de estado
  private mapEstadoNombreToId(nombre: string): number {
    const map: Record<string, number> = {
      'PENDIENTE': 1,
      'EN PROCESO': 2,
      'APROBADA': 3,
      'ATENDIDA': 4,
      'CERRADA': 5,
      'RECHAZADA': 6,
      'ANULADA': 7
    };
    return map[nombre] ?? 1;
  }

  // ──────────────────────────────────────────────
  //               Form Modal
  // ──────────────────────────────────────────────

  abrirModalCrear(): void {
    this.selectedOc = null;
    this.isEditMode = false;
    this.showFormModal = true;
  }

  onFormSubmitted(success: boolean): void {
    this.showFormModal = false;
    if (success) {
      this.cargarOrdenes(this.currentPage);
    }
  }

  // SweetAlert helpers
  private mostrarExito(mensaje: string): void {
    Swal.fire({ icon: 'success', title: 'Éxito', text: mensaje, timer: 2200, showConfirmButton: false });
  }

  private mostrarError(mensaje: string): void {
    Swal.fire({ icon: 'error', title: 'Error', text: mensaje });
  }

  getEstadoClass(estado: string): string {
    const map: Record<string, string> = {
      'APROBADA': 'bg-success',
      'PENDIENTE': 'bg-warning text-dark',
      'EN PROCESO': 'bg-primary',
      'ATENDIDA': 'bg-info',
      'CERRADA': 'bg-secondary',
      'RECHAZADA': 'bg-danger',
      'ANULADA': 'bg-dark'
    };
    return map[estado] || 'bg-secondary';
  }

  getTotal(oc: OrdenCompraResponse): number {
    return oc.cantidad * oc.costoUnitario;
  }
}
