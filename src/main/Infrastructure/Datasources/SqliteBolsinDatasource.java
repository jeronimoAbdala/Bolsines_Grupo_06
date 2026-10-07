package main.Infrastructure.Datasources;

import main.Domain.Entities.Bolsin;
import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.DetalleRemito;
import main.Domain.Entities.Documentation;
import main.Domain.Entities.Estado;
import main.Domain.Entities.Remito;
import main.Domain.Entities.TipoDocumento;
import main.Infrastructure.Database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SqliteBolsinDatasource {

    private final Connection conn;

    public SqliteBolsinDatasource(Connection conn) {
        this.conn = conn;
    }

    public SqliteBolsinDatasource() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<Bolsin> obtenerTodos() {
        List<Bolsin> bolsines = new ArrayList<>();
        String sql = "SELECT * FROM bolsin";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Bolsin b = mapearBolsin(rs);
                if (b != null) bolsines.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bolsines;
    }

    public List<Bolsin> obtenerEnviadosHacia(ComisionMedica cmDestino) {
        List<Bolsin> bolsines = new ArrayList<>();
        String sql = "SELECT * FROM bolsin WHERE cm_destino_codigo = ? AND estado_nombre = 'ENVIADO'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cmDestino.getCodigo());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Bolsin b = mapearBolsin(rs);
                if (b != null) bolsines.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bolsines;
    }

    public Bolsin buscarPorNumero(int numero) {
        String sql = "SELECT * FROM bolsin WHERE numero = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, numero);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearBolsin(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void guardar(Bolsin bolsin) {
        String sql = """
            INSERT OR REPLACE INTO bolsin
            (numero, fecha_generacion, nro_precinto, cant_remitos, peso,
             estado_ambito, estado_nombre, cm_origen_codigo, cm_destino_codigo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bolsin.getNumero());
            pstmt.setString(2, bolsin.getFechaGeneracion().toString());
            pstmt.setString(3, bolsin.getNroPrecinto());
            pstmt.setInt(4, bolsin.getCantRemitos());
            pstmt.setDouble(5, bolsin.getPeso());
            pstmt.setString(6, bolsin.getEstado().getAmbito());
            pstmt.setString(7, bolsin.getEstado().getNombre());
            pstmt.setInt(8, bolsin.getCmOrigen().getCodigo());
            pstmt.setInt(9, bolsin.getCmDestino().getCodigo());
            pstmt.executeUpdate();
            guardarRemitos(bolsin);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void crearBolsin(int numero, String precinto, double peso,
                            ComisionMedica cmOrigen, ComisionMedica cmDestino) {
        Estado estado = new Estado("BOL", "ENVIADO", "Bolsín enviado");
        Bolsin bolsin = new Bolsin(numero, cmOrigen, cmDestino, estado);
        bolsin.setNroPrecinto(precinto);
        bolsin.setPeso(peso);
        guardar(bolsin);
    }

    public void agregarRemitoABolsin(int bolsinNumero, Remito remito) {
        Bolsin bolsin = buscarPorNumero(bolsinNumero);
        if (bolsin != null) {
            bolsin.agregarRemito(remito);
            guardar(bolsin);
        }
    }

    public List<ComisionMedica> obtenerComisionesMedicas() {
        List<ComisionMedica> cms = new ArrayList<>();
        String sql = "SELECT * FROM comision_medica";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cms.add(new ComisionMedica(
                        rs.getInt("codigo"), rs.getString("nombre"),
                        rs.getString("direccion"), rs.getString("email"),
                        rs.getString("telefono")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cms;
    }

    public ComisionMedica crearComisionMedica(int codigo, String nombre, String direccion, String email, String telefono) {
        ComisionMedica cm = new ComisionMedica(codigo, nombre, direccion, email, telefono);
        String sql = "INSERT OR REPLACE INTO comision_medica (codigo, nombre, direccion, email, telefono) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cm.getCodigo());
            pstmt.setString(2, cm.getNombre());
            pstmt.setString(3, cm.getDireccion());
            pstmt.setString(4, cm.getEmail());
            pstmt.setString(5, cm.getTelefono());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cm;
    }

    public int obtenerSiguienteNumeroBolsin() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(numero), 1000) + 1 FROM bolsin")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1001;
    }

    public int obtenerSiguienteNumeroRemito() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(numero), 500) + 1 FROM remito")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 501;
    }

    public int obtenerSiguienteNumeroDocumentacion() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(numero), 0) + 1 FROM documentacion")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void guardarRemitos(Bolsin bolsin) {
        String sqlDel = "DELETE FROM detalle_remito WHERE remito_numero IN (SELECT numero FROM remito WHERE bolsin_numero = ?)";
        String sqlDelR = "DELETE FROM remito WHERE bolsin_numero = ?";
        try (PreparedStatement p1 = conn.prepareStatement(sqlDel);
             PreparedStatement p2 = conn.prepareStatement(sqlDelR)) {
            p1.setInt(1, bolsin.getNumero());
            p1.executeUpdate();
            p2.setInt(1, bolsin.getNumero());
            p2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sqlRemito = """
            INSERT OR REPLACE INTO remito
            (numero, fecha, estado_ambito, estado_nombre, cm_origen_codigo, cm_destino_codigo, bolsin_numero)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        String sqlDetalle = """
            INSERT INTO detalle_remito (area_cm_destino, remito_numero, documentacion_numero)
            VALUES (?, ?, ?)
        """;
        String sqlDoc = """
            INSERT OR REPLACE INTO documentacion
            (numero, fecha_pase, asunto, descripcion, estado_ambito, estado_nombre,
             tipo_documento_nombre, tipo_documento_descripcion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            for (Remito remito : bolsin.getRemitos()) {
                try (PreparedStatement pr = conn.prepareStatement(sqlRemito)) {
                    pr.setInt(1, remito.getNumero());
                    pr.setString(2, remito.getFecha().toString());
                    pr.setString(3, remito.getEstado().getAmbito());
                    pr.setString(4, remito.getEstado().getNombre());
                    pr.setInt(5, remito.getCmOrigen().getCodigo());
                    pr.setInt(6, remito.getCmDestino().getCodigo());
                    pr.setInt(7, bolsin.getNumero());
                    pr.executeUpdate();
                }
                for (DetalleRemito detalle : remito.getDetalles()) {
                    Documentation doc = detalle.getDocumentacion();
                    try (PreparedStatement pd = conn.prepareStatement(sqlDoc)) {
                        pd.setInt(1, doc.getNumero());
                        pd.setString(2, doc.getFechaPase().toString());
                        pd.setString(3, doc.getAsunto());
                        pd.setString(4, doc.getDescripcion());
                        pd.setString(5, doc.getEstado().getAmbito());
                        pd.setString(6, doc.getEstado().getNombre());
                        pd.setString(7, doc.getTipoDocumento().getNombre());
                        pd.setString(8, doc.getTipoDocumento().getDescripcion());
                        pd.executeUpdate();
                    }
                    try (PreparedStatement pdet = conn.prepareStatement(sqlDetalle)) {
                        pdet.setString(1, detalle.getAreaCMDestino());
                        pdet.setInt(2, remito.getNumero());
                        pdet.setInt(3, doc.getNumero());
                        pdet.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Bolsin mapearBolsin(ResultSet rs) throws SQLException {
        int numero = rs.getInt("numero");
        String estadoAmbito = rs.getString("estado_ambito");
        String estadoNombre = rs.getString("estado_nombre");
        Estado estado = new Estado(estadoAmbito, estadoNombre, "");

        int cmOrigenCodigo = rs.getInt("cm_origen_codigo");
        int cmDestinoCodigo = rs.getInt("cm_destino_codigo");
        ComisionMedica cmOrigen = buscarComisionMedica(cmOrigenCodigo);
        ComisionMedica cmDestino = buscarComisionMedica(cmDestinoCodigo);

        if (cmOrigen == null || cmDestino == null) return null;

        Bolsin bolsin = new Bolsin(numero, cmOrigen, cmDestino, estado);
        String precinto = rs.getString("nro_precinto");
        if (precinto != null) bolsin.setNroPrecinto(precinto);
        bolsin.setPeso(rs.getDouble("peso"));

        List<Remito> remitos = buscarRemitosDeBolsin(numero);
        for (Remito remito : remitos) {
            bolsin.agregarRemito(remito);
        }

        return bolsin;
    }

    private ComisionMedica buscarComisionMedica(int codigo) {
        String sql = "SELECT * FROM comision_medica WHERE codigo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ComisionMedica(
                        rs.getInt("codigo"),
                        rs.getString("nombre"),
                        rs.getString("direccion"),
                        rs.getString("email"),
                        rs.getString("telefono")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Remito> buscarRemitosDeBolsin(int bolsinNumero) {
        List<Remito> remitos = new ArrayList<>();
        String sql = "SELECT * FROM remito WHERE bolsin_numero = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bolsinNumero);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Remito remito = mapearRemito(rs);
                    if (remito != null) remitos.add(remito);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return remitos;
    }

    private Remito mapearRemito(ResultSet rs) throws SQLException {
        int numero = rs.getInt("numero");
        LocalDate fecha = LocalDate.parse(rs.getString("fecha"));
        String estadoAmbito = rs.getString("estado_ambito");
        String estadoNombre = rs.getString("estado_nombre");
        Estado estado = new Estado(estadoAmbito, estadoNombre, "");

        int cmOrigenCodigo = rs.getInt("cm_origen_codigo");
        int cmDestinoCodigo = rs.getInt("cm_destino_codigo");
        ComisionMedica cmOrigen = buscarComisionMedica(cmOrigenCodigo);
        ComisionMedica cmDestino = buscarComisionMedica(cmDestinoCodigo);

        if (cmOrigen == null || cmDestino == null) return null;

        Remito remito = new Remito(numero, fecha, estado, cmOrigen, cmDestino);

        String sqlDetalle = """
            SELECT dr.*, d.asunto, d.descripcion, d.estado_ambito as doc_estado_ambito,
                   d.estado_nombre as doc_estado_nombre, d.tipo_documento_nombre,
                   d.tipo_documento_descripcion, d.fecha_pase
            FROM detalle_remito dr
            JOIN documentacion d ON dr.documentacion_numero = d.numero
            WHERE dr.remito_numero = ?
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sqlDetalle)) {
            pstmt.setInt(1, numero);
            try (ResultSet rsd = pstmt.executeQuery()) {
                while (rsd.next()) {
                    Estado docEstado = new Estado(
                            rsd.getString("doc_estado_ambito"),
                            rsd.getString("doc_estado_nombre"), "");
                    TipoDocumento tipo = new TipoDocumento(
                            rsd.getString("tipo_documento_nombre"),
                            rsd.getString("tipo_documento_descripcion"));
                    Documentation doc = new Documentation(
                            rsd.getInt("documentacion_numero"),
                            rsd.getString("asunto"),
                            rsd.getString("descripcion"),
                            docEstado, tipo);
                    remito.agregarDetalle(new DetalleRemito(doc, rsd.getString("area_cm_destino")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return remito;
    }

    public void cargarDatosDePrueba() {
        try {
            try (Statement check = conn.createStatement();
                 ResultSet rs = check.executeQuery("SELECT COUNT(*) as cnt FROM comision_medica")) {
                if (rs.next() && rs.getInt("cnt") > 0) return;
            }

            conn.setAutoCommit(false);
            try {
                ComisionMedica cordoba = new ComisionMedica(1, "Cordoba", "Colon 100", "cm@correo.com", "351-123456");
                ComisionMedica buenosAires = new ComisionMedica(2, "Buenos Aires", "Av. 9 de Julio 100", "cmba@correo.com", "11-654321");

                insertarComisionMedica(cordoba);
                insertarComisionMedica(buenosAires);

                insertarEmpleado("Jeronimo", "Abdala", "mail@mail.com", 1);
                int empId = obtenerUltimoEmpleadoId();
                insertarUsuario("jero", "1234", empId);

                Estado estadoEnviadoBol = new Estado("BOL", "ENVIADO", "Bolsín enviado");
                Estado estadoEnviadoRem = new Estado("REM", "ENVIADO", "Remito enviado");
                Estado estadoEnviadaDoc = new Estado("DOC", "ENVIADA", "Documentación enviada");

                Bolsin b1 = new Bolsin(1001, buenosAires, cordoba, estadoEnviadoBol);
                b1.setNroPrecinto("PREC-001");
                b1.agregarRemito(crearRemito(500, LocalDate.now(), buenosAires, cordoba, 1,
                        "Expediente-cardiológico", "Historia clínica del paciente X", estadoEnviadoRem, estadoEnviadaDoc));
                b1.agregarRemito(crearRemito(501, LocalDate.now(), buenosAires, cordoba, 2,
                        "Estudios-complementarios", "Radiografías y análisis", estadoEnviadoRem, estadoEnviadaDoc));
                guardar(b1);

                Bolsin b2 = new Bolsin(1002, buenosAires, cordoba, estadoEnviadoBol);
                b2.setNroPrecinto("PREC-002");
                b2.agregarRemito(crearRemito(502, LocalDate.now(), buenosAires, cordoba, 3,
                        "Turno-derivación", "Documentación para derivación", estadoEnviadoRem, estadoEnviadaDoc));
                guardar(b2);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertarComisionMedica(ComisionMedica cm) throws SQLException {
        String sql = "INSERT INTO comision_medica (codigo, nombre, direccion, email, telefono) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cm.getCodigo());
            pstmt.setString(2, cm.getNombre());
            pstmt.setString(3, cm.getDireccion());
            pstmt.setString(4, cm.getEmail());
            pstmt.setString(5, cm.getTelefono());
            pstmt.executeUpdate();
        }
    }

    private void insertarEmpleado(String nombre, String apellido, String mail, int cmCodigo) throws SQLException {
        String sql = "INSERT INTO empleado (nombre, apellido, mail, comision_medica_codigo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, apellido);
            pstmt.setString(3, mail);
            pstmt.setInt(4, cmCodigo);
            pstmt.executeUpdate();
        }
    }

    private void insertarUsuario(String nombreUsuario, String password, int empleadoId) throws SQLException {
        String sql = "INSERT INTO usuario (nombre_usuario, password, empleado_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombreUsuario);
            pstmt.setString(2, password);
            pstmt.setInt(3, empleadoId);
            pstmt.executeUpdate();
        }
    }

    private int obtenerUltimoEmpleadoId() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 1;
    }

    private Remito crearRemito(int numero, LocalDate fecha,
                               ComisionMedica cmOrigen, ComisionMedica cmDestino,
                               int docId, String asunto, String desc,
                               Estado estadoRem, Estado estadoDoc) {
        Remito remito = new Remito(numero, fecha, estadoRem, cmOrigen, cmDestino);
        Documentation doc = new Documentation(docId, asunto, desc, estadoDoc,
                new TipoDocumento("EXPEDIENTE", "Expediente médico"));
        remito.agregarDetalle(new DetalleRemito(doc));
        return remito;
    }
}
