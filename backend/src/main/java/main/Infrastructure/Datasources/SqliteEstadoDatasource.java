package main.Infrastructure.Datasources;

import main.Domain.Entities.Estado;
import main.Infrastructure.Database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteEstadoDatasource {

    private final Connection conn;

    public SqliteEstadoDatasource(Connection conn) {
        this.conn = conn;
    }

    public SqliteEstadoDatasource() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<Estado> obtenerTodos() {
        List<Estado> estados = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM estado")) {
            while (rs.next()) {
                estados.add(new Estado(rs.getString("ambito"), rs.getString("nombre"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return estados;
    }

    public void cargarDatosDePrueba() {
        try (Statement check = conn.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) FROM estado")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String sql = "INSERT INTO estado (ambito, nombre, descripcion) VALUES (?, ?, ?)";
        String[][] estados = {
                {"BOL", "ENVIADO", "Bolsín enviado"},
                {"BOL", "RECIBIDO", "Bolsín recibido en comisión destino"},
                {"REM", "ENVIADO", "Remito enviado"},
                {"REM", "RECIBIDO_ACEPTADO", "Remito recibido y aceptado"},
                {"DOC", "ENVIADA", "Documentación enviada"},
                {"DOC", "RECIBIDA_ACEPTADA", "Documentación recibida y aceptada"}
        };
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String[] estado : estados) {
                pstmt.setString(1, estado[0]);
                pstmt.setString(2, estado[1]);
                pstmt.setString(3, estado[2]);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
