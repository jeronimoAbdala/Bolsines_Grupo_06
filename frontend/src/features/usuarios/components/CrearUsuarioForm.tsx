import { useState, type FormEvent } from 'react';
import { Alert, Button, Field } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useComisiones } from '@/features/comisiones';
import { useCrearUsuario } from '../hooks/useUsuarios';

const VACIO = { nombreUsuario: '', password: '', nombre: '', apellido: '', mail: '', comisionMedicaCodigo: '' };

export const CrearUsuarioForm = () => {
  const [form, setForm] = useState(VACIO);
  const { data: comisiones = [] } = useComisiones();
  const crear = useCrearUsuario();

  const set = (campo: keyof typeof VACIO) => (e: { target: { value: string } }) =>
    setForm({ ...form, [campo]: e.target.value });

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    crear.mutate(
      { ...form, comisionMedicaCodigo: Number(form.comisionMedicaCodigo) },
      { onSuccess: () => setForm(VACIO) },
    );
  };

  return (
    <form className="form" onSubmit={enviar}>
      <Field label="Nombre de usuario" htmlFor="u-usuario">
        <input id="u-usuario" required value={form.nombreUsuario} onChange={set('nombreUsuario')} />
      </Field>
      <Field label="Password" htmlFor="u-password">
        <input id="u-password" type="password" required value={form.password} onChange={set('password')} />
      </Field>
      <Field label="Nombre" htmlFor="u-nombre">
        <input id="u-nombre" required value={form.nombre} onChange={set('nombre')} />
      </Field>
      <Field label="Apellido" htmlFor="u-apellido">
        <input id="u-apellido" required value={form.apellido} onChange={set('apellido')} />
      </Field>
      <Field label="Mail" htmlFor="u-mail">
        <input id="u-mail" type="email" value={form.mail} onChange={set('mail')} />
      </Field>
      <Field label="Comision medica" htmlFor="u-cm">
        <select id="u-cm" required value={form.comisionMedicaCodigo} onChange={set('comisionMedicaCodigo')}>
          <option value="">Seleccionar…</option>
          {comisiones.map((cm) => <option key={cm.codigo} value={cm.codigo}>{cm.nombre}</option>)}
        </select>
      </Field>
      {crear.isError && <Alert tone="error">{errorMessage(crear.error)}</Alert>}
      {crear.isSuccess && <Alert tone="success">Usuario "{crear.data.nombreUsuario}" creado.</Alert>}
      <div className="acciones">
        <Button type="submit" loading={crear.isPending}>Crear usuario</Button>
      </div>
    </form>
  );
};
