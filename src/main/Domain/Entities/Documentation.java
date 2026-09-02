package main.Domain.Entities;

public class Documentation<Estado> {

    private int numero;
    private final String asunto;
    private String descripcion;

    private Estado estado;
    private TipoDocumento tipoDocumento;

    public Documentation(int numero,
                         String asunto,
                         String descripcion,
                         Estado estado,
                         TipoDocumento tipoDocumento) {

        this.numero = numero;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.estado = estado;
        this.tipoDocumento = tipoDocumento;
    }

    public String getAsunto() {
        return asunto;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
