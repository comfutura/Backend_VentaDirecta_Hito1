// src/app/services/cargo-solicitante.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environment';
import { CargoSolicitante } from '../model/cargo-solicitante.interface';

@Injectable({
  providedIn: 'root'
})
export class CargoSolicitanteService {

  constructor(private http: HttpClient) {}

  private getApiUrl(tipo: 'ANALISTA' | 'JEFATURA'): string {
    const recurso = tipo === 'ANALISTA' ? 'analista-cliente-solicitante' : 'jefatura-cliente-solicitante';
    return `${environment.baseUrl}/api/${recurso}`;
  }

  guardar(cargo: CargoSolicitante): Observable<CargoSolicitante> {
    const url = this.getApiUrl(cargo.tipo);
    return this.http.post<CargoSolicitante>(url, cargo);
  }

  listar(tipo: 'ANALISTA' | 'JEFATURA'): Observable<CargoSolicitante[]> {
    const url = this.getApiUrl(tipo);
    return this.http.get<CargoSolicitante[]>(url);
  }

  toggle(id: number, tipo: 'ANALISTA' | 'JEFATURA'): Observable<void> {
    const url = this.getApiUrl(tipo);
    return this.http.post<void>(`${url}/${id}/toggle`, null);
  }

  // Opcional: listar TODOS (si el backend lo permite en el futuro)
  listarTodos(): Observable<CargoSolicitante[]> {
    // Si en algún momento el backend tiene un endpoint /api/cargos-solicitantes
    // Por ahora podemos concatenar las dos llamadas
    return new Observable(observer => {
      this.listar('ANALISTA').subscribe({
        next: analistas => {
          this.listar('JEFATURA').subscribe({
            next: jefaturas => {
              observer.next([...analistas, ...jefaturas]);
              observer.complete();
            },
            error: err => observer.error(err)
          });
        },
        error: err => observer.error(err)
      });
    });
  }
}
