// src/app/pages/form-ots/form-ots.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, tap } from 'rxjs';
import Swal from 'sweetalert2';

import { DropdownItem, DropdownService } from '../../../service/dropdown.service';
import { AuthService } from '../../../service/auth.service';
import { OtService } from '../../../service/ot.service';
import { CrearOtCompletaRequest, OtCreateRequest, OtResponse } from '../../../model/ots';

@Component({
  selector: 'app-form-ots-component',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './form-ots-component.html',
  styleUrl: './form-ots-component.css'
})
export class FormOtsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private otService = inject(OtService);
  private dropdownService = inject(DropdownService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form!: FormGroup;
  submitted = false;
  loading = true;
  isEditMode = false;
  otId: number | null = null;
  otNumber: string | null = null;

  usernameLogueado: string = '—';
  trabajadorIdLogueado: number | null = null;

  // Dropdowns
  clientes: DropdownItem[] = [];
  areas: DropdownItem[] = [];
  proyectos: DropdownItem[] = [];
  fases: DropdownItem[] = [];
  sites: DropdownItem[] = [];
  regiones: DropdownItem[] = [];

  jefaturasCliente: DropdownItem[] = [];
  analistasCliente: DropdownItem[] = [];
  coordinadoresTiCw: DropdownItem[] = [];
  jefaturasResponsable: DropdownItem[] = [];
  liquidadores: DropdownItem[] = [];
  ejecutantes: DropdownItem[] = [];
  analistasContable: DropdownItem[] = [];

  constructor() {
    this.crearFormularioBase();
  }

  ngOnInit(): void {
    const user = this.authService.currentUser;
    this.usernameLogueado = user?.username || '—';
    this.trabajadorIdLogueado = user?.idTrabajador ?? null;

    // Detectar modo edición
    const idParam = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!idParam && !isNaN(Number(idParam));
    this.otId = this.isEditMode ? Number(idParam) : null;

    // Cargar todo lo necesario
    if (this.isEditMode && this.otId) {
      this.cargarDatosParaEdicion(this.otId);
    } else {
      this.cargarDropdownsParaCreacion();
    }

    // Suscripciones reactivas
    this.suscribirCambiosFormulario();
  }

  private crearFormularioBase(): void {
    const hoy = new Date().toISOString().split('T')[0];

    this.form = this.fb.group({
      idOts: [null],
      idCliente: [null, Validators.required],
      idArea: [{ value: null, disabled: true }, Validators.required],
      idProyecto: [null, Validators.required],
      idFase: [null, Validators.required],
      idSite: [null, Validators.required],
      idRegion: [null, Validators.required],
      descripcion: ['', Validators.required],
      fechaApertura: [hoy, Validators.required],

      idJefaturaClienteSolicitante: [null],
      idAnalistaClienteSolicitante: [null],
      idCoordinadorTiCw: [null],
      idJefaturaResponsable: [null],
      idLiquidador: [null],
      idEjecutante: [null],
      idAnalistaContable: [null],

      idOtsAnterior: [null, [Validators.min(1), Validators.pattern('^[0-9]+$')]]
    });

    // En modo creación la descripción se genera automáticamente
    if (!this.isEditMode) {
      this.form.get('descripcion')?.disable();
    }
  }

  private suscribirCambiosFormulario(): void {
    // Cascada: áreas según cliente
    this.form.get('idCliente')?.valueChanges.subscribe(clienteId => {
      const areaCtrl = this.form.get('idArea');
      if (clienteId) {
        this.cargarAreasPorCliente(Number(clienteId));
        areaCtrl?.enable({ emitEvent: false });
      } else {
        this.areas = [];
        areaCtrl?.disable({ emitEvent: false });
        areaCtrl?.setValue(null, { emitEvent: false });
      }
    });

    // Generación automática de descripción (solo creación)
    if (!this.isEditMode) {
      ['idProyecto', 'idArea', 'idSite'].forEach(field => {
        this.form.get(field)?.valueChanges.subscribe(() => this.actualizarDescripcion());
      });
    }
  }

  private cargarDropdownsParaCreacion(): void {
    this.cargarTodosLosCatalogos().subscribe({
      next: () => {
        this.loading = false;
      },
      error: () => {
        Swal.fire('Error', 'No se pudieron cargar los catálogos necesarios', 'error');
        this.loading = false;
      }
    });
  }

  private cargarDatosParaEdicion(id: number): void {
    forkJoin({
      ot: this.otService.getOtById(id),
      catalogs: this.cargarTodosLosCatalogos()
    }).subscribe({
      next: ({ ot }) => {
        this.otNumber = ot.ot?.toString() ?? null;

        this.form.patchValue({
          idOts: ot.idOts,
          idCliente: this.findIdByLabel(this.clientes, ot.clienteRazonSocial),
          idArea: this.findIdByLabel(this.areas, ot.areaNombre),
          idProyecto: this.findIdByLabel(this.proyectos, ot.proyectoNombre),
          idFase: this.findIdByLabel(this.fases, ot.faseNombre),
          idSite: this.findIdByLabel(this.sites, ot.siteNombre),
          idRegion: this.findIdByLabel(this.regiones, ot.regionNombre),
          descripcion: ot.descripcion || '',
          fechaApertura: ot.fechaApertura ? ot.fechaApertura.split('T')[0] : '',
          idJefaturaClienteSolicitante: this.findIdByLabel(this.jefaturasCliente, ot.jefaturaClienteSolicitante),
          idAnalistaClienteSolicitante: this.findIdByLabel(this.analistasCliente, ot.analistaClienteSolicitante),
          idCoordinadorTiCw: this.findIdByLabel(this.coordinadoresTiCw, ot.coordinadorTiCw),
          idJefaturaResponsable: this.findIdByLabel(this.jefaturasResponsable, ot.jefaturaResponsable),
          idLiquidador: this.findIdByLabel(this.liquidadores, ot.liquidador),
          idEjecutante: this.findIdByLabel(this.ejecutantes, ot.ejecutante),
          idAnalistaContable: this.findIdByLabel(this.analistasContable, ot.analistaContable),
          idOtsAnterior: null  // Si lo necesitas, agrégalo al backend
        });

        // Forzar recarga de áreas si hay cliente seleccionado
        const clienteId = this.form.get('idCliente')?.value;
        if (clienteId) {
          this.cargarAreasPorCliente(clienteId);
        }

        this.loading = false;
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo cargar la información de la OT para edición',
        });
        this.router.navigate(['/ot']);
      }
    });
  }

  private cargarTodosLosCatalogos() {
    return forkJoin({
      clientes: this.dropdownService.getClientes(),
      proyectos: this.dropdownService.getProyectos(),
      fases: this.dropdownService.getFases(),
      sites: this.dropdownService.getSites(),
      regiones: this.dropdownService.getRegiones(),
      jefaturasCliente: this.dropdownService.getJefaturasClienteSolicitante(),
      analistasCliente: this.dropdownService.getAnalistasClienteSolicitante(),
      coordinadoresTiCw: this.dropdownService.getCoordinadoresTiCw(),
      jefaturasResp: this.dropdownService.getJefaturasResponsable(),
      liquidadores: this.dropdownService.getLiquidador(),
      ejecutantes: this.dropdownService.getEjecutantes(),
      analistasCont: this.dropdownService.getAnalistasContable()
    }).pipe(
      tap(data => {
        this.clientes = data.clientes || [];
        this.proyectos = data.proyectos || [];
        this.fases = data.fases || [];
        this.sites = data.sites || [];
        this.regiones = data.regiones || [];
        this.jefaturasCliente = data.jefaturasCliente || [];
        this.analistasCliente = data.analistasCliente || [];
        this.coordinadoresTiCw = data.coordinadoresTiCw || [];
        this.jefaturasResponsable = data.jefaturasResp || [];
        this.liquidadores = data.liquidadores || [];
        this.ejecutantes = data.ejecutantes || [];
        this.analistasContable = data.analistasCont || [];
      })
    );
  }

  private cargarAreasPorCliente(idCliente: number): void {
    this.dropdownService.getAreasByCliente(idCliente).subscribe({
      next: (areas) => {
        this.areas = areas || [];
        // Si ya hay un valor de área en el form y no está en la nueva lista → limpiarlo
        const areaActual = this.form.get('idArea')?.value;
        if (areaActual && !this.areas.some(a => a.id === areaActual)) {
          this.form.get('idArea')?.setValue(null, { emitEvent: false });
        }
      },
      error: () => {
        this.areas = [];
        Swal.fire('Atención', 'No se pudieron cargar las áreas del cliente', 'warning');
      }
    });
  }

  private findIdByLabel(items: DropdownItem[], label?: string | null): number | null {
    if (!label) return null;
    return items.find(item => item.label === label)?.id ?? null;
  }

  private actualizarDescripcion(): void {
    if (this.isEditMode) return;

    const v = this.form.getRawValue();

    const proyecto = this.proyectos.find(p => p.id === Number(v.idProyecto))?.label || '';
    const area     = this.areas.find(a => a.id === Number(v.idArea))?.label || '';
    const site     = this.sites.find(s => s.id === Number(v.idSite))?.label || '';

    const toSlug = (str: string) =>
      str
        .toLowerCase()
        .trim()
        .replace(/\s+/g, '_')
        .replace(/[^a-z0-9_]/g, '')
        .replace(/_+/g, '_')
        .replace(/^_+|_+$/g, '');

    const partes = [
      toSlug(proyecto),
      toSlug(area),
      v.idSite ? String(v.idSite) : '',
      toSlug(site)
    ].filter(Boolean);

    const desc = partes.join('_') || 'OT sin descripción automática';
    this.form.get('descripcion')?.setValue(desc, { emitEvent: false });
  }

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.form.markAllAsTouched();

    if (this.form.invalid) {
      Swal.fire({
        icon: 'warning',
        title: 'Formulario incompleto',
        text: 'Por favor completa los campos obligatorios.',
        confirmButtonColor: '#ffc107'
      });
      return;
    }

    const title = this.isEditMode ? '¿Guardar cambios?' : '¿Crear nueva OT?';
    Swal.fire({
      title,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#6c757d',
      confirmButtonText: this.isEditMode ? 'Sí, guardar' : 'Sí, crear',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) return;

      this.loading = true;

      const values = this.form.getRawValue();

      const otPayload: OtCreateRequest = {
        idOts: this.isEditMode ? Number(values.idOts) : undefined,
        idCliente: Number(values.idCliente),
        idArea: Number(values.idArea),
        idProyecto: Number(values.idProyecto),
        idFase: Number(values.idFase),
        idSite: Number(values.idSite),
        idRegion: Number(values.idRegion),
        descripcion: (values.descripcion || '').trim(),
        fechaApertura: values.fechaApertura,
        idOtsAnterior: values.idOtsAnterior ? Number(values.idOtsAnterior) : null,
        idJefaturaClienteSolicitante: values.idJefaturaClienteSolicitante || null,
        idAnalistaClienteSolicitante: values.idAnalistaClienteSolicitante || null,
        idCoordinadorTiCw: values.idCoordinadorTiCw || null,
        idJefaturaResponsable: values.idJefaturaResponsable || null,
        idLiquidador: values.idLiquidador || null,
        idEjecutante: values.idEjecutante || null,
        idAnalistaContable: values.idAnalistaContable || null,
      };

      const payload: CrearOtCompletaRequest = {
        ot: otPayload,
        trabajadores: [],   // ← implementa si manejas asignación de trabajadores
        // detalles: []     // si usas ítems/materiales
      };

      this.otService.saveOtCompleta(payload).subscribe({
        next: (res: OtResponse) => {
          Swal.fire({
            icon: 'success',
            title: this.isEditMode ? '¡OT actualizada!' : '¡OT creada con éxito!',
            html: `Número OT: <strong>#${res.ot}</strong>`,
            timer: 2800,
            timerProgressBar: true,
            showConfirmButton: false
          });
          setTimeout(() => this.router.navigate(['/ot', res.idOts]), 2800);
        },
        error: (err) => {
          Swal.fire({
            icon: 'error',
            title: 'Error al guardar',
            text: err.error?.message || 'Ocurrió un problema inesperado',
          });
          this.loading = false;
        }
      });
    });
  }

  resetForm(): void {
    Swal.fire({
      title: this.isEditMode ? '¿Descartar cambios?' : '¿Limpiar formulario?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc3545',
      cancelButtonText: 'Cancelar',
      confirmButtonText: this.isEditMode ? 'Sí, descartar' : 'Sí, limpiar'
    }).then(result => {
      if (!result.isConfirmed) return;

      if (this.isEditMode) {
        this.router.navigate(['/ot']);
      } else {
        this.form.reset();
        this.submitted = false;
        this.areas = [];
        this.form.get('idArea')?.disable();
        this.form.patchValue({
          fechaApertura: new Date().toISOString().split('T')[0]
        });
        this.actualizarDescripcion();
      }
    });
  }
}
