// src/app/components/gestion-cargos-solicitantes/gestion-cargos-solicitantes.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CargoSolicitante } from '../../model/cargo-solicitante.interface';
import { CargoSolicitanteService } from '../../service/cargo-solicitante.service';

type ModoVista = 'ANALISTA' | 'JEFATURA';

@Component({
  selector: 'app-gestion-cargos-solicitantes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-cargos-solicitantes-component.html',
  styleUrl: './gestion-cargos-solicitantes-component.css',
})
export class GestionCargosSolicitantesComponent implements OnInit {

  modo: ModoVista = 'ANALISTA'; // valor inicial (puedes cambiar a 'JEFATURA' si prefieres)

  cargos: CargoSolicitante[] = [];
  cargosOriginal: CargoSolicitante[] = [];

  searchDescripcion = '';

  showForm = false;
  isEditMode = false;
  currentCargo: CargoSolicitante = { descripcion: '', activo: true, tipo: 'ANALISTA' };

  isLoading = false;
  errorMessage: string | null = null;

  constructor(private cargoService: CargoSolicitanteService) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cambiarModo(nuevoModo: ModoVista): void {
    this.modo = nuevoModo;
    this.cargarDatos();
    this.cancelForm();
  }

  private cargarDatos(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // Ya no hay 'TODOS', siempre cargamos por modo específico
    this.cargoService.listar(this.modo).subscribe({
      next: data => this.procesarDatos(data),
      error: err => this.manejarError(err)
    });
  }

  private procesarDatos(data: any[]): void {
    // Como ya no hay modo TODOS, siempre forzamos el tipo según la pestaña actual
    this.cargosOriginal = data.map(item => ({
      ...item,
      tipo: this.modo
    }));

    this.applyFilter();
    this.isLoading = false;
  }

  private manejarError(err: any): void {
    this.errorMessage = 'Error al cargar los datos';
    console.error(err);
    this.isLoading = false;
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
      tipo: this.modo  // siempre coincide con la pestaña actual
    };
    this.showForm = true;
    this.errorMessage = null;
  }

  openEditForm(cargo: CargoSolicitante): void {
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

    // Ya no necesitamos esta validación porque tipo siempre coincide con modo
    // if (this.currentCargo.tipo !== this.modo) { ... }

    this.isLoading = true;

    this.cargoService.guardar(this.currentCargo).subscribe({
      next: () => {
        this.showForm = false;
        this.cargarDatos();
        this.isLoading = false;
      },
      error: err => {
        this.errorMessage = 'Error al guardar';
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  toggleActivo(cargo: CargoSolicitante): void {
    if (!cargo.id) return;

    this.isLoading = true;

    this.cargoService.toggle(cargo.id, this.modo).subscribe({
      next: () => {
        const idx = this.cargos.findIndex(c => c.id === cargo.id);
        if (idx !== -1) {
          this.cargos[idx].activo = !this.cargos[idx].activo;
        }
        const idxOrig = this.cargosOriginal.findIndex(c => c.id === cargo.id);
        if (idxOrig !== -1) {
          this.cargosOriginal[idxOrig].activo = this.cargos[idx].activo;
        }
        this.isLoading = false;
      },
      error: err => {
        this.errorMessage = 'No se pudo cambiar el estado';
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  cancelForm(): void {
    this.showForm = false;
    this.errorMessage = null;
  }

  getTitulo(): string {
    return this.modo === 'ANALISTA'
      ? 'Gestión de Analistas Cliente Solicitante'
      : 'Gestión de Jefaturas Cliente Solicitante';
  }
}
