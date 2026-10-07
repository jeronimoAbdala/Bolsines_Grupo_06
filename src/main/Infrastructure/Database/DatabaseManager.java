package main.Infrastructure.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:bolsines.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                try (Statement s = connection.createStatement()) {
                    s.execute("PRAGMA busy_timeout = 5000");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al conectar con SQLite", e);
        }
        return connection;
    }

    public void inicializar() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("PRAGMA busy_timeout = 5000");
            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS comision_medica (
                    codigo INTEGER PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    direccion TEXT,
                    email TEXT,
                    telefono TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS empleado (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    apellido TEXT NOT NULL,
                    mail TEXT,
                    comision_medica_codigo INTEGER,
                    FOREIGN KEY (comision_medica_codigo) REFERENCES comision_medica(codigo)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuario (
                    nombre_usuario TEXT PRIMARY KEY,
                    password TEXT NOT NULL,
                    empleado_id INTEGER,
                    FOREIGN KEY (empleado_id) REFERENCES empleado(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS estado (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ambito TEXT NOT NULL,
                    nombre TEXT NOT NULL,
                    descripcion TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bolsin (
                    numero INTEGER PRIMARY KEY,
                    fecha_generacion TEXT,
                    nro_precinto TEXT,
                    cant_remitos INTEGER DEFAULT 0,
                    peso REAL DEFAULT 0,
                    estado_ambito TEXT,
                    estado_nombre TEXT,
                    cm_origen_codigo INTEGER,
                    cm_destino_codigo INTEGER,
                    FOREIGN KEY (cm_origen_codigo) REFERENCES comision_medica(codigo),
                    FOREIGN KEY (cm_destino_codigo) REFERENCES comision_medica(codigo)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS remito (
                    numero INTEGER PRIMARY KEY,
                    fecha TEXT,
                    estado_ambito TEXT,
                    estado_nombre TEXT,
                    cm_origen_codigo INTEGER,
                    cm_destino_codigo INTEGER,
                    bolsin_numero INTEGER,
                    FOREIGN KEY (bolsin_numero) REFERENCES bolsin(numero)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS detalle_remito (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    area_cm_destino TEXT,
                    remito_numero INTEGER,
                    documentacion_numero INTEGER,
                    FOREIGN KEY (remito_numero) REFERENCES remito(numero)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS documentacion (
                    numero INTEGER PRIMARY KEY,
                    fecha_pase TEXT,
                    asunto TEXT,
                    descripcion TEXT,
                    estado_ambito TEXT,
                    estado_nombre TEXT,
                    tipo_documento_nombre TEXT,
                    tipo_documento_descripcion TEXT
                )
            """);

        } catch (SQLException e) {
            throw new RuntimeException("Error al crear tablas", e);
        }
    }

    public void cerrar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
