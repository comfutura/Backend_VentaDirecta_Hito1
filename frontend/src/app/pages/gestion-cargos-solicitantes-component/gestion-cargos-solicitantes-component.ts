import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

import { CargoSolicitante } from '../../model/cargo-solicitante.interface';
import { CargoSolicitanteService } from '../../service/cargo-solicitante.service';

type ModoVista = 'ANALISTA' | 'JEFATURA';

@Component({
  selector: 'app-gestion-cargos-solicitantes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-cargos-solicitantes-component.html',
  styleUrls: ['./gestion-cargos-solicitantes-component.css']
})
export class GestionCargosSolicitantesComponent implements OnInit {
  modo: ModoVista = 'ANALISTA';
  
  cargos: CargoSolicitante[] = [];
  cargosOriginal: CargoSolicitante[] = [];
  
  searchDescripcion = '';
  
  showForm = false;
  isEditMode = false;
  currentCargo: CargoSolicitante = { 
    descripcion: '', 
    activo: true, 
    tipo: 'ANALISTA' 
  };
  
  isLoading = false;
  errorMessage: string | null = null;

  constructor(private cargoService: CargoSolicitanteService) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cambiarModo(nuevoModo: ModoVista): void {
    if (this.modo === nuevoModo) return;
    
    this.modo = nuevoModo;
    this.cargarDatos();
    this.cancelForm();
    
    // Scroll suave al inicio
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private cargarDatos(): void {
    this.isLoading = true;
    this.errorMessage = null;
    
    this.cargoService.listar(this.modo).subscribe({
      next: data => this.procesarDatos(data),
      error: err => this.manejarError(err)
    });
  }

  private procesarDatos(data: any[]): void {
    this.cargosOriginal = data.map(item => ({
      ...item,
      tipo: this.modo
    }));
    
    this.applyFilter();
    this.isLoading = false;
  }

  private manejarError(err: any): void {
    this.errorMessage = 'Error al cargar los datos. Por favor, intente nuevamente.';
    console.error('Error cargando cargos:', err);
    this.isLoading = false;
    
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: 'No se pudieron cargar los datos',
      confirmButtonColor: '#ef4444'
    });
  }

  applyFilter(): void {
    const term = this.searchDescripcion.trim().toLowerCase();
    if (!term) {
      this.cargos = [...this.cargosOriginal];
      return;
    }
    this.cargos = this.cargosOriginal.filter(c =>
      c.descripcion.toLowerCase().includes(term)
    );
  }

  onSearchChange(): void {
    this.applyFilter();
  }

  openCreateForm(): void {
    this.isEditMode = false;
    this.currentCargo = {
      descripcion: '',
      activo: true,
      tipo: this.modo
    };
    this.showForm = true;
    this.errorMessage = null;
  }

  openEditModal(cargo: CargoSolicitante): void {
    this.isEditMode = true;
    this.currentCargo = { ...cargo };
    this.showForm = true;
    this.errorMessage = null;
  }

  saveCargo(): void {
    if (!this.currentCargo.descripcion?.trim()) {
      this.errorMessage = 'La descripción es obligatoria';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    this.cargoService.guardar(this.currentCargo).subscribe({
      next: (response) => {
        this.showForm = false;
        
        Swal.fire({
          icon: 'success',
          title: '¡Guardado exitoso!',
          text: this.isEditMode 
            ? 'El cargo ha sido actualizado correctamente.'
            : 'El nuevo cargo ha sido creado correctamente.',
          timer: 2000,
          showConfirmButton: false,
          timerProgressBar: true
        });
        
        this.cargarDatos();
        this.isLoading = false;
      },
      error: err => {
        this.errorMessage = err?.error?.message || 'Error al guardar el cargo.';
        console.error('Error guardando cargo:', err);
        this.isLoading = false;
      
      }
    });
  }

  toggleActivo(cargo: CargoSolicitante): void {
    if (!cargo.id) return;

    const accion = cargo.activo ? 'desactivar' : 'activar';
    const titulo = cargo.activo ? 'Desactivar Cargo' : 'Activar Cargo';
    const texto = cargo.activo 
      ? '¿Está seguro de que desea desactivar este cargo? No estará disponible para nuevas asignaciones.'
      : '¿Está seguro de que desea activar este cargo? Estará disponible para nuevas asignaciones.';

    Swal.fire({
      title: titulo,
      text: texto,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: cargo.activo ? '#ef4444' : '#10b981',
      cancelButtonColor: '#6b7280',
      confirmButtonText: cargo.activo ? 'Sí, desactivar' : 'Sí, activar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.isLoading = true;
        
        this.cargoService.toggle(cargo.id!, this.modo).subscribe({
          next: () => {
            cargo.activo = !cargo.activo;
            
            Swal.fire({
              icon: 'success',
              title: '¡Estado actualizado!',
              text: `El cargo ha sido ${accion}do correctamente.`,
              timer: 1500,
              showConfirmButton: false
            });
            
            this.isLoading = false;
          },
          error: err => {
            this.errorMessage = `No se pudo ${accion} el cargo`;
            console.error(`Error ${accion}do cargo:`, err);
            this.isLoading = false;
            
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: this.errorMessage,
              confirmButtonColor: '#ef4444'
            });
          }
        });
      }
    });
  }

  cancelForm(): void {
    this.showForm = false;
    this.errorMessage = null;
  }

  // Métodos auxiliares para la vista
  getContador(tipo: ModoVista): number {
    return this.cargosOriginal.filter(c => c.tipo === tipo).length;
  }

  getCargoIcon(cargo: CargoSolicitante): string {
    return cargo.tipo === 'ANALISTA' ? 'bi-person-badge' : 'bi-person-vcard';
  }

  getCargoIconClass(cargo: CargoSolicitante): string {
    return cargo.tipo === 'ANALISTA' ? 'analista' : 'jefatura';
  }

  getTipoBadgeClass(cargo: CargoSolicitante): string {
    return cargo.tipo === 'ANALISTA' ? 'analista' : 'jefatura';
  }

  // Para compatibilidad con el template existente
  getTitulo(): string {
    return this.modo === 'ANALISTA'
      ? 'Gestión de Analistas Cliente Solicitante'
      : 'Gestión de Jefaturas Cliente Solicitante';
  }
  
  // Método alias para el template
  openEditForm(cargo: CargoSolicitante): void {
    this.openEditModal(cargo);
  }
}