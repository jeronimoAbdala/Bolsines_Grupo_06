import { useState, type FormEvent } from 'react';
import { Alert, Button, Field } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useCrearComision } from '../hooks/useComisiones';

const VACIO = { codigo: '', nombre: '', direccion: '', email: '', telefono: '' };

export const CrearComisionForm = () => {
  const [form, setForm] = useState(VACIO);
  const crear = useCrearComision();

  const set = (campo: keyof typeof VACIO) => (e: { target: { value: string } }) =>
    setForm({ ...form, [campo]: e.target.value });

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    crear.mutate({ ...form, codigo: Number(form.codigo) }, { onSuccess: () => setForm(VACIO) });
  };

  return (
    <form className="form" onSubmit={enviar}>
      <Field label="Codigo" htmlFor="cm-codigo">
        <input id="cm-codigo" type="number" min="1" required value={form.codigo} onChange={set('codigo')} />
      </Field>
      <Field label="Nombre" htmlFor="cm-nombre">
        <input id="cm-nombre" required value={form.nombre} onChange={set('nombre')} />
      </Field>
      <Field label="Direccion" htmlFor="cm-direccion">
        <input id="cm-direccion" value={form.direccion} onChange={set('direccion')} />
      </Field>
      <Field label="Email" htmlFor="cm-email">
        <input id="cm-email" type="email" value={form.email} onChange={set('email')} />
      </Field>
      <Field label="Telefono" htmlFor="cm-telefono">
        <input id="cm-telefono" value={form.telefono} onChange={set('telefono')} />
      </Field>
      {crear.isError && <Alert tone="error">{errorMessage(crear.error)}</Alert>}
      {crear.isSuccess && <Alert tone="success">Comision "{crear.data.nombre}" creada.</Alert>}
      <div className="acciones">
        <Button type="submit" loading={crear.isPending}>Crear comision</Button>
      </div>
    </form>
  );
};
