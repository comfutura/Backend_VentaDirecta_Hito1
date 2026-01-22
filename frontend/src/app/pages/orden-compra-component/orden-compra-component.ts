import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { OrdenCompraService } from '../../service/orden-compra.service';
import { OrdenCompraResponse, PageOrdenCompra } from '../../model/orden-compra.model';

@Component({
  selector: 'app-orden-compra-component',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orden-compra-component.html',
  styleUrl: './orden-compra-component.css'
})
export class OrdenCompraComponent implements OnInit {

  // Datos
  pageData: PageOrdenCompra | null = null;
  ordenes: OrdenCompraResponse[] = [];
  filteredOrdenes: OrdenCompraResponse[] = [];

  // Filtros
  searchTerm: string = '';
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 1;

  // Estados UI
  isLoading: boolean = false;
  errorMessage: string | null = null;

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
        this.errorMessage = 'No se pudieron cargar las órdenes de compra';
        console.error(err);
        this.mostrarError(this.errorMessage);
        this.isLoading = false;
      }
    });
  }

  // Filtro local (búsqueda en frontend)
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
      (oc.observacion && oc.observacion.toLowerCase().includes(term))
    );
  }

  onSearchChange(): void {
    this.aplicarFiltro();
  }

  // Navegación paginación
  irAPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPages) {
      this.cargarOrdenes(pagina);
    }
  }

  // Acciones
  eliminarOrden(id: number): void {
    Swal.fire({
      title: '¿Estás seguro?',
      text: 'Esta orden de compra será eliminada permanentemente',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.ordenService.eliminar(id).subscribe({
          next: () => {
            this.mostrarExito('Orden eliminada correctamente');
            this.cargarOrdenes(this.currentPage);
          },
          error: () => this.mostrarError('No se pudo eliminar la orden')
        });
      }
    });
  }

  // Helpers SweetAlert
  private mostrarExito(mensaje: string): void {
    Swal.fire({
      icon: 'success',
      title: '¡Éxito!',
      text: mensaje,
      timer: 2500,
      showConfirmButton: false
    });
  }

  private mostrarError(mensaje: string): void {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: mensaje,
      confirmButtonText: 'Aceptar'
    });
  }

  // Helpers visuales
  getEstadoClass(estado: string): string {
    const map: Record<string, string> = {
      'APROBADA': 'bg-success',
      'PENDIENTE': 'bg-warning',
      'RECHAZADA': 'bg-danger',
      'ANULADA': 'bg-secondary',
      'CERRADA': 'bg-info',
      'EN PROCESO': 'bg-primary',
      'ATENDIDA': 'bg-success'
    };
    return map[estado] || 'bg-secondary';
  }

  getTotal(oc: OrdenCompraResponse): number {
    return oc.cantidad * oc.costoUnitario;
  }
}
