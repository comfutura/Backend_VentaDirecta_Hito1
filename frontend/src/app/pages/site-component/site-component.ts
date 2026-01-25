import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import Swal from 'sweetalert2';

import { Site } from '../../model/site.interface';
import { SiteService } from '../../service/site.service';
import { Page } from '../../model/ots';

@Component({
  selector: 'app-site',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './site-component.html',
  styleUrls: ['./site-component.css']
})
export class SiteComponent implements OnInit, OnDestroy {
  // Datos principales
  sites: Site[] = [];
  filteredSites: Site[] = [];
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // Filtro con debounce
  searchDescripcion: string = '';
  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  // Formulario modal
  showModal = false;
  isEditMode = false;
  currentSite: Site = { codigoSitio: '', descripcion: '', activo: true };
  formSubmitted = false;

  // Loading y mensajes
  isLoading = false;
  isTableLoading = false;
  errorMessage: string = '';

  // Paginación visible
  visiblePages: number[] = [];

  constructor(private siteService: SiteService) {}

  ngOnInit(): void {
    this.setupSearchDebounce();
    this.loadSites();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  private setupSearchDebounce(): void {
    this.searchSubscription = this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.onSearchChange();
      });
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchDescripcion);
  }

  loadSites(page: number = this.currentPage): void {
    this.isTableLoading = true;
    this.errorMessage = '';

    this.siteService.listar(page, this.pageSize).subscribe({
      next: (response: Page<Site>) => {
        this.sites = response.content;
        this.filteredSites = [...this.sites];
        this.currentPage = response.number;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.updateVisiblePages();
        this.isTableLoading = false;

        // Aplicar filtro si hay búsqueda activa
        if (this.searchDescripcion.trim()) {
          this.applyFilter();
        }
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar los sitios. Por favor, intente nuevamente.';
        console.error('Error cargando sitios:', err);
        this.isTableLoading = false;
        
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar los sitios',
          confirmButtonColor: '#ef4444'
        });
      }
    });
  }

  applyFilter(): void {
    const term = this.searchDescripcion.toLowerCase().trim();
    
    if (!term) {
      this.filteredSites = [...this.sites];
      return;
    }

    this.filteredSites = this.sites.filter(site =>
      (site.descripcion || '').toLowerCase().includes(term) ||
      (site.codigoSitio || '').toLowerCase().includes(term) ||
      (site.idSite?.toString() || '').includes(term)
    );
  }

  onSearchChange(): void {
    if (this.searchDescripcion.trim() === '') {
      this.filteredSites = [...this.sites];
    } else {
      this.applyFilter();
    }
  }

  clearSearch(): void {
    this.searchDescripcion = '';
    this.filteredSites = [...this.sites];
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.formSubmitted = false;
    this.currentSite = { 
      codigoSitio: '', 
      descripcion: '', 
      activo: true 
    };
    this.showModal = true;
    this.errorMessage = '';
  }

  openEditModal(site: Site): void {
    this.isEditMode = true;
    this.formSubmitted = false;
    this.currentSite = { ...site };
    this.showModal = true;
    this.errorMessage = '';
  }

  saveSite(): void {
    this.formSubmitted = true;

    if (!this.currentSite.codigoSitio.trim()) {
      this.errorMessage = 'El código de sitio es obligatorio';
      return;
    }

    if (this.currentSite.codigoSitio.length > 20) {
      this.errorMessage = 'El código no puede exceder los 20 caracteres';
      return;
    }

    if (this.currentSite.descripcion && this.currentSite.descripcion.length > 150) {
      this.errorMessage = 'La descripción no puede exceder los 150 caracteres';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.siteService.guardar(this.currentSite).subscribe({
      next: () => {
        this.showModal = false;
        
        Swal.fire({
          icon: 'success',
          title: '¡Guardado exitoso!',
          text: this.isEditMode 
            ? 'El sitio ha sido actualizado correctamente.'
            : 'El nuevo sitio ha sido creado correctamente.',
          timer: 2000,
          showConfirmButton: false,
          timerProgressBar: true
        });
        
        this.loadSites(this.currentPage);
        this.isLoading = false;
      },
      error: (err) => {
        const errorMessage = err?.error?.message || 'Error al guardar el sitio.';
        this.errorMessage = errorMessage;
        console.error('Error guardando sitio:', err);
        this.isLoading = false;
        
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: errorMessage,
          confirmButtonColor: '#ef4444'
        });
      }
    });
  }

  closeModal(): void {
    this.showModal = false;
    this.formSubmitted = false;
    this.errorMessage = '';
  }

  toggleActivo(site: Site): void {
    if (!site.idSite) {
      this.errorMessage = 'El sitio no tiene un ID válido';
      return;
    }

    const accion = site.activo ? 'desactivar' : 'activar';
    const titulo = site.activo ? 'Desactivar Sitio' : 'Activar Sitio';
    const texto = site.activo 
      ? '¿Está seguro de que desea desactivar este sitio? No estará disponible para nuevas asignaciones.'
      : '¿Está seguro de que desea activar este sitio? Estará disponible para nuevas asignaciones.';

    Swal.fire({
      title: titulo,
      text: texto,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: site.activo ? '#ef4444' : '#10b981',
      cancelButtonColor: '#6b7280',
      confirmButtonText: site.activo ? 'Sí, desactivar' : 'Sí, activar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.isLoading = true;
        
        // Aseguramos que idSite es number
        const siteId = site.idSite as number;
        
        this.siteService.toggle(siteId).subscribe({
          next: () => {
            site.activo = !site.activo;
            
            Swal.fire({
              icon: 'success',
              title: '¡Estado actualizado!',
              text: `El sitio ha sido ${accion}do correctamente.`,
              timer: 1500,
              showConfirmButton: false
            });
            
            this.isLoading = false;
          },
          error: (err) => {
            const errorMessage = `No se pudo ${accion} el sitio`;
            this.errorMessage = errorMessage;
            console.error(`Error ${accion}do sitio:`, err);
            this.isLoading = false;
            
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: err?.error?.message || errorMessage,
              confirmButtonColor: '#ef4444'
            });
          }
        });
      }
    });
  }

  // Paginación
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.loadSites(page);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.goToPage(this.currentPage - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.goToPage(this.currentPage + 1);
    }
  }

  updateVisiblePages(): void {
    const maxVisible = 5;
    const start = Math.max(0, Math.min(
      this.currentPage - Math.floor(maxVisible / 2),
      this.totalPages - maxVisible
    ));
    const end = Math.min(start + maxVisible, this.totalPages);
    
    this.visiblePages = Array.from(
      { length: end - start }, 
      (_, i) => start + i
    );
  }

  // Métodos auxiliares para la vista
  getStatusBadgeClass(activo?: boolean): string {
    return activo ? 'bg-success' : 'bg-secondary';
  }

  getStatusText(activo?: boolean): string {
    return activo ? 'Activo' : 'Inactivo';
  }

  getStatusIcon(activo?: boolean): string {
    return activo ? 'bi-check-circle' : 'bi-x-circle';
  }

  getTotalDisplayed(): number {
    return this.filteredSites.length;
  }

  getCurrentRange(): string {
    const start = this.currentPage * this.pageSize + 1;
    const end = Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
    return `${start}-${end}`;
  }

  // Métodos para contar sitios activos/inactivos
  getActiveSitesCount(): number {
    return this.sites.filter(s => s.activo === true).length;
  }

  getInactiveSitesCount(): number {
    return this.sites.filter(s => s.activo === false).length;
  }

  // Método para manejar el cambio de página desde el input
  getInputPage(event: Event): number {
    const input = event.target as HTMLInputElement;
    const page = parseInt(input.value, 10) - 1;
    return isNaN(page) ? this.currentPage : Math.max(0, Math.min(page, this.totalPages - 1));
  }
}