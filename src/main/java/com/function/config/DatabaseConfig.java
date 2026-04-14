package com.function.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Configuración de conexión a Oracle Cloud Database.
 * Gestiona el pool de conexiones HikariCP con soporte para Oracle Wallet.
 */
public class DatabaseConfig {
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());
    private static HikariDataSource dataSource;

    /**
     * Determina si la aplicación está ejecutándose en Azure.
     * 
     * @return true si está en Azure, false en caso contrario
     */
    private static boolean isAzureEnvironment() {
        return System.getenv("WEBSITE_INSTANCE_ID") != null;
    }

    static {
        try {
            initializeDataSource();
        } catch (Exception e) {
            logger.severe("Error initializing database connection pool: " + e.getMessage());
            throw new RuntimeException("Error initializing database connection pool", e);
        }
    }

    /**
     * Inicializa el pool de conexiones HikariCP.
     * Configura la conexión a Oracle Cloud Database con Oracle Wallet.
     */
    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();

        String tnsAdmin = isAzureEnvironment()
                ? "/home/site/wwwroot/Wallet_DCNII"
                : "./Wallet_DCNII";

        String serviceName = "dcnii_tp";
        String username = "ADMIN";
        String password = "DesarrolloCN2.";

        System.setProperty("oracle.net.tns_admin", tnsAdmin);
        System.setProperty("oracle.net.wallet_location", tnsAdmin);

        String jdbcUrl = "jdbc:oracle:thin:@" + serviceName;

        logger.info("Conectando a Oracle Cloud Database...");
        logger.info("TNS Admin: " + tnsAdmin);
        logger.info("Service Name: " + serviceName);
        logger.info("Usuario: " + username);

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("oracle.jdbc.OracleDriver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 segundos
        config.setIdleTimeout(600000); // 10 minutos
        config.setMaxLifetime(1800000); // 30 minutos
        config.setConnectionTestQuery("SELECT 1 FROM DUAL");
        config.setPoolName("OracleCloudPool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        config.addDataSourceProperty("oracle.jdbc.fanEnabled", "false");
        config.addDataSourceProperty("oracle.net.ssl_version", "1.2");
        config.addDataSourceProperty("oracle.net.ssl_server_dn_match", "true");

        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection()) {
            logger.info("✓ Conexión exitosa a Oracle Cloud Database");
        } catch (SQLException e) {
            logger.severe("✗ Error al verificar conexión: " + e.getMessage());
            throw new RuntimeException("No se pudo conectar a Oracle Cloud Database", e);
        }
    }

    /**
     * Obtiene el DataSource configurado con el pool de conexiones.
     * 
     * @return DataSource con pool de conexiones HikariCP
     * @throws IllegalStateException si el DataSource no está inicializado o está
     *                               cerrado
     */
    public static DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("DataSource no está inicializado o está cerrado");
        }
        return dataSource;
    }

    /**
     * Obtiene una conexión del pool de conexiones.
     * 
     * @return conexión activa desde el pool
     * @throws SQLException si hay error al obtener la conexión
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Cierra el pool de conexiones.
     * Útil para testing o apagado de la aplicación.
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Cerrando pool de conexiones...");
            dataSource.close();
        }
    }

    /**
     * Obtiene estadísticas actuales del pool de conexiones.
     * 
     * @return String con información del estado del pool
     */
    public static String getPoolStats() {
        if (dataSource == null) {
            return "DataSource no inicializado";
        }
        return String.format(
                "Pool Stats - Total: %d, Activas: %d, Inactivas: %d, Esperando: %d",
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
}
