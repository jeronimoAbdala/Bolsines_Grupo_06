import { useState, type FormEvent } from 'react';
import { Alert, Button, Card, Field, PageHeader } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useAgregarRemito, useBolsines } from '../hooks/useBolsines';
import { TIPOS_DOCUMENTO, type TipoDocumento } from '../types/bolsin.types';

interface FormState {
  bolsin: string;
  asunto: string;
  descripcion: string;
  tipoDocumento: TipoDocumento;
}

const VACIO: FormState = { bolsin: '', asunto: '', descripcion: '', tipoDocumento: 'EXPEDIENTE' };

export const AgregarRemitoPage = () => {
  const [form, setForm] = useState<FormState>(VACIO);
  const { data: bolsines = [] } = useBolsines();
  const agregar = useAgregarRemito();
  const elegido = bolsines.find((b) => String(b.numero) === form.bolsin);

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    const { bolsin, ...data } = form;
    agregar.mutate({ numero: Number(bolsin), data }, { onSuccess: () => setForm({ ...VACIO, bolsin }) });
  };

  return (
    <>
      <PageHeader title="Agregar remito" subtitle="Crea un remito con un documento y lo suma al bolsin elegido." />
      <Card>
        <form className="form" onSubmit={enviar}>
          <Field
            label="Bolsin"
            htmlFor="bolsin"
            hint={elegido ? `${elegido.cmOrigen.nombre} → ${elegido.cmDestino.nombre} · ${elegido.estado}` : undefined}
          >
            <select id="bolsin" required value={form.bolsin} onChange={(e) => setForm({ ...form, bolsin: e.target.value })}>
              <option value="">Seleccionar…</option>
              {bolsines.map((b) => <option key={b.numero} value={b.numero}>Bolsin #{b.numero}</option>)}
            </select>
          </Field>
          <Field label="Asunto" htmlFor="asunto">
            <input id="asunto" required value={form.asunto} onChange={(e) => setForm({ ...form, asunto: e.target.value })} />
          </Field>
          <Field label="Descripcion" htmlFor="descripcion">
            <textarea id="descripcion" rows={3} value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} />
          </Field>
          <Field label="Tipo de documento" htmlFor="tipo">
            <select id="tipo" value={form.tipoDocumento} onChange={(e) => setForm({ ...form, tipoDocumento: e.target.value as TipoDocumento })}>
              {TIPOS_DOCUMENTO.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </Field>
          {agregar.isError && <Alert tone="error">{errorMessage(agregar.error)}</Alert>}
          {agregar.isSuccess && <Alert tone="success">Remito agregado al Bolsin #{agregar.data.numero}.</Alert>}
          <div className="acciones">
            <Button type="submit" loading={agregar.isPending}>Agregar remito</Button>
          </div>
        </form>
      </Card>
    </>
  );
};
