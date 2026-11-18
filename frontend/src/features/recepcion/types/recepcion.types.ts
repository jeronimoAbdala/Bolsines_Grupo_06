// --- JSON de los DTO del backend que usa el CU 28 (ComisionMedicaDto, UsuarioDto, DocumentacionDto, RemitoDto, BolsinDto).

export interface ComisionMedica {
  codigo: number;
  nombre: string;
  direccion: string;
  email: string;
  telefono: string;
}

/** Usuario logueado con su comision medica (paso 2). */
export interface Usuario {
  nombreUsuario: string;
  nombre: string;
  apellido: string;
  mail: string;
  comisionMedica: ComisionMedica;
}

export interface Documento {
  numero: number;
  asunto: string;
  descripcion: string;
  tipo: string;
  estado: string;
}

export interface Remito {
  numero: number;
  fecha: string;
  estado: string;
  documentos: Documento[];
}

/** Encabezado del bolsin seleccionado (paso 5) y bolsin recibido (paso 12). */
export interface Bolsin {
  numero: number;
  fechaGeneracion: string;
  nroPrecinto: string | null;
  peso: number;
  cantRemitos: number;
  estado: string;
  cmOrigen: ComisionMedica;
  cmDestino: ComisionMedica;
  remitos: Remito[];
}

// --- Respuestas de PantallaRegistrarRecepcionBolsin.

/** Paso 4: una fila de la lista (la arma la Pantalla del backend; el precinto llega 'N/A' si no hay). */
export interface BolsinParaSeleccion {
  numero: number;
  cmOrigen: string;
  nroPrecinto: string;
  cantRemitos: number;
  estado: string;
}

/** Respuesta de POST /recepcion/iniciar: usuario logueado con su CM (paso 2), bolsines para seleccionar y el texto que pide elegir uno (paso 4). */
export interface VentanaRecepcion {
  usuario: Usuario;
  bolsines: BolsinParaSeleccion[];
  solicitudSeleccion: string;
}

/** Respuesta de POST /recepcion/seleccion/{n}: encabezado, texto del Iterator (paso 6) y las 4 opciones (paso 7). */
export interface DetalleRecepcion {
  bolsin: Bolsin;
  informacionRemito: string;
  opcionesRecepcion: string[];
}
