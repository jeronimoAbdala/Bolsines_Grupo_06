export const formatFecha = (iso: string): string => {
  const [anio, mes, dia] = iso.split('-');
  return `${dia}/${mes}/${anio}`;
};

export const formatPeso = (kg: number): string => `${kg.toLocaleString('es-AR')} kg`;
