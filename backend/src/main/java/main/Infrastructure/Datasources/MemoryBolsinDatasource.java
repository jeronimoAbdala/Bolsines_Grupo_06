package main.Infrastructure.Datasources;

import main.Domain.Entities.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemoryBolsinDatasource implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String ARCHIVO_DATOS = "data/bolsines.dat";

    private final List<Bolsin> bolsines;

    private final Estado estadoEnviadoBol = new Estado("BOL", "ENVIADO", "Bolsín enviado desde comisión de origen");
    private final Estado estadoEnviadoRem = new Estado("REM", "ENVIADO", "Remito enviado");
    private final Estado estadoEnviadaDoc = new Estado("DOC", "ENVIADA", "Documentación enviada");

    public MemoryBolsinDatasource() {
        this.bolsines = new ArrayList<>();
        cargar();
    }

    public List<Bolsin> obtenerTodos() {
        return bolsines;
    }

    public void guardar(Bolsin bolsin) {
        bolsines.add(bolsin);
        persistir();
    }

    private void cargar() {
        File archivo = new File(ARCHIVO_DATOS);
        if (archivo.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                @SuppressWarnings("unchecked")
                List<Bolsin> cargados = (List<Bolsin>) ois.readObject();
                bolsines.addAll(cargados);
                return;
            } catch (Exception ignored) {
            }
        }
        cargarDatosDePrueba();
    }

    private void persistir() {
        try {
            File directorio = new File("data");
            if (!directorio.exists()) {
                directorio.mkdirs();
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
                oos.writeObject(new ArrayList<>(bolsines));
            }
        } catch (IOException ignored) {
        }
    }

    private void cargarDatosDePrueba() {
        ComisionMedica cordoba = new ComisionMedica(1, "Cordoba", "Colon 100", "cm@correo.com", "351-123456");
        ComisionMedica buenosAires = new ComisionMedica(2, "Buenos Aires", "Av. 9 de Julio 100", "cmba@correo.com", "11-654321");

        Bolsin bolsin1001 = new Bolsin(1001, buenosAires, cordoba, estadoEnviadoBol);
        bolsin1001.setNroPrecinto("PREC-001");
        bolsin1001.agregarRemito(crearRemito(500, LocalDate.now(), buenosAires, cordoba,
                1, "Expediente-cardiológico", "Historia clínica del paciente X"));
        bolsin1001.agregarRemito(crearRemito(501, LocalDate.now(), buenosAires, cordoba,
                2, "Estudios-complementarios", "Radiografías y análisis"));
        guardar(bolsin1001);

        Bolsin bolsin1002 = new Bolsin(1002, buenosAires, cordoba, estadoEnviadoBol);
        bolsin1002.setNroPrecinto("PREC-002");
        bolsin1002.agregarRemito(crearRemito(502, LocalDate.now(), buenosAires, cordoba,
                3, "Turno-derivación", "Documentación para derivación"));
        guardar(bolsin1002);
    }

    private Remito crearRemito(int numeroRemito, LocalDate fecha,
                               ComisionMedica cmOrigen, ComisionMedica cmDestino,
                               int documentacionId, String asunto, String descripcion) {
        Remito remito = new Remito(numeroRemito, fecha, estadoEnviadoRem, cmOrigen, cmDestino);
        Documento documento = new Documento(
                documentacionId,
                asunto,
                descripcion,
                estadoEnviadaDoc,
                new TipoDocumento("EXPEDIENTE", "Expediente médico")
        );
        remito.agregarDetalle(new DetalleRemito(documento));
        return remito;
    }
}
