import { useState, type FormEvent } from 'react';
import { Alert, Button, Card, Field, PageHeader } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useComisiones } from '@/features/comisiones';
import { useCrearBolsin } from '../hooks/useBolsines';

const VACIO = { nroPrecinto: '', peso: '0', cmOrigenCodigo: '', cmDestinoCodigo: '' };

export const CrearBolsinPage = () => {
  const [form, setForm] = useState(VACIO);
  const { data: comisiones = [] } = useComisiones();
  const crear = useCrearBolsin();

  const set = (campo: keyof typeof VACIO) => (e: { target: { value: string } }) =>
    setForm({ ...form, [campo]: e.target.value });

  const mismaComision = form.cmOrigenCodigo !== '' && form.cmOrigenCodigo === form.cmDestinoCodigo;

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    crear.mutate(
      {
        nroPrecinto: form.nroPrecinto,
        peso: Number(form.peso),
        cmOrigenCodigo: Number(form.cmOrigenCodigo),
        cmDestinoCodigo: Number(form.cmDestinoCodigo),
      },
      { onSuccess: () => setForm(VACIO) },
    );
  };

  return (
    <>
      <PageHeader title="Nuevo bolsin" subtitle="El bolsin se crea en estado ENVIADO con un numero automatico." />
      <Card>
        <form className="form" onSubmit={enviar}>
          <Field label="Precinto" htmlFor="precinto">
            <input id="precinto" required value={form.nroPrecinto} onChange={set('nroPrecinto')} />
          </Field>
          <Field label="Peso (kg)" htmlFor="peso">
            <input id="peso" type="number" min="0" step="0.1" value={form.peso} onChange={set('peso')} />
          </Field>
          <Field label="Comision origen" htmlFor="origen">
            <select id="origen" required value={form.cmOrigenCodigo} onChange={set('cmOrigenCodigo')}>
              <option value="">Seleccionar…</option>
              {comisiones.map((cm) => <option key={cm.codigo} value={cm.codigo}>{cm.nombre}</option>)}
            </select>
          </Field>
          <Field label="Comision destino" htmlFor="destino" hint={mismaComision ? 'Debe ser distinta del origen.' : undefined}>
            <select id="destino" required value={form.cmDestinoCodigo} onChange={set('cmDestinoCodigo')}>
              <option value="">Seleccionar…</option>
              {comisiones.map((cm) => <option key={cm.codigo} value={cm.codigo}>{cm.nombre}</option>)}
            </select>
          </Field>
          {crear.isError && <Alert tone="error">{errorMessage(crear.error)}</Alert>}
          {crear.isSuccess && <Alert tone="success">Bolsin #{crear.data.numero} creado.</Alert>}
          <div className="acciones">
            <Button type="submit" loading={crear.isPending} disabled={mismaComision}>Crear bolsin</Button>
          </div>
        </form>
      </Card>
    </>
  );
};
