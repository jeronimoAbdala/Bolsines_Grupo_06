package main.Infrastructure.Datasources;

import main.Domain.Entities.ComisionMedica;
import main.Domain.Entities.Empleado;
import main.Domain.Entities.Usuario;
import main.Infrastructure.Database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteUsuarioDatasource {

    private final Connection conn;

    public SqliteUsuarioDatasource(Connection conn) {
        this.conn = conn;
    }

    public SqliteUsuarioDatasource() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<Usuario> obtenerTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = """
            SELECT u.nombre_usuario, u.password,
                   e.nombre as emp_nombre, e.apellido as emp_apellido, e.mail as emp_mail,
                   cm.codigo as cm_codigo, cm.nombre as cm_nombre, cm.direccion as cm_direccion,
                   cm.email as cm_email, cm.telefono as cm_telefono
            FROM usuario u
            JOIN empleado e ON u.empleado_id = e.id
            JOIN comision_medica cm ON e.comision_medica_codigo = cm.codigo
        """;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ComisionMedica cm = new ComisionMedica(
                        rs.getInt("cm_codigo"), rs.getString("cm_nombre"),
                        rs.getString("cm_direccion"), rs.getString("cm_email"),
                        rs.getString("cm_telefono"));
                Empleado emp = new Empleado(
                        rs.getString("emp_nombre"), rs.getString("emp_apellido"),
                        rs.getString("emp_mail"), cm);
                Usuario u = new Usuario(rs.getString("nombre_usuario"), rs.getString("password"), emp);
                usuarios.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    public Usuario buscarPorNombre(String nombreUsuario) {
        String sql = """
            SELECT u.nombre_usuario, u.password,
                   e.nombre as emp_nombre, e.apellido as emp_apellido, e.mail as emp_mail,
                   cm.codigo as cm_codigo, cm.nombre as cm_nombre, cm.direccion as cm_direccion,
                   cm.email as cm_email, cm.telefono as cm_telefono
            FROM usuario u
            JOIN empleado e ON u.empleado_id = e.id
            JOIN comision_medica cm ON e.comision_medica_codigo = cm.codigo
            WHERE u.nombre_usuario = ?
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombreUsuario);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ComisionMedica cm = new ComisionMedica(
                        rs.getInt("cm_codigo"), rs.getString("cm_nombre"),
                        rs.getString("cm_direccion"), rs.getString("cm_email"),
                        rs.getString("cm_telefono"));
                Empleado emp = new Empleado(
                        rs.getString("emp_nombre"), rs.getString("emp_apellido"),
                        rs.getString("emp_mail"), cm);
                return new Usuario(rs.getString("nombre_usuario"), rs.getString("password"), emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void crearUsuario(String nombreUsuario, String password, Empleado empleado) {
        try {
            conn.setAutoCommit(false);
            try {
                String sqlEmp = "INSERT INTO empleado (nombre, apellido, mail, comision_medica_codigo) VALUES (?, ?, ?, ?)";
                try (PreparedStatement p = conn.prepareStatement(sqlEmp)) {
                    p.setString(1, empleado.getNombre());
                    p.setString(2, empleado.getApellido());
                    p.setString(3, empleado.getMail());
                    p.setInt(4, empleado.getComisionMedica().getCodigo());
                    p.executeUpdate();
                }

                int empId;
                try (Statement s = conn.createStatement();
                     ResultSet rs = s.executeQuery("SELECT last_insert_rowid()")) {
                    empId = rs.next() ? rs.getInt(1) : 1;
                }

                String sqlUsr = "INSERT INTO usuario (nombre_usuario, password, empleado_id) VALUES (?, ?, ?)";
                try (PreparedStatement p = conn.prepareStatement(sqlUsr)) {
                    p.setString(1, nombreUsuario);
                    p.setString(2, password);
                    p.setInt(3, empId);
                    p.executeUpdate();
                }

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
        String sql = "INSERT OR REPLACE INTO comision_medica (codigo, nombre, direccion, email, telefono) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, codigo);
            pstmt.setString(2, nombre);
            pstmt.setString(3, direccion);
            pstmt.setString(4, email);
            pstmt.setString(5, telefono);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ComisionMedica(codigo, nombre, direccion, email, telefono);
    }
}
