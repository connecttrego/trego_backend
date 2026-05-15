import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixOrdersVendorId {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        String[] statements = {
            // Drop the FK, modify the column, re-add FK
            "ALTER TABLE orders DROP FOREIGN KEY FKb1443sk0bxprtkqree3o2qk90",
            "ALTER TABLE orders MODIFY COLUMN vendor_id INT",
            "ALTER TABLE orders ADD CONSTRAINT FKb1443sk0bxprtkqree3o2qk90 FOREIGN KEY (vendor_id) REFERENCES vendor_informations(vendor_user_id)"
        };

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                try {
                    stmt.executeUpdate(sql);
                    System.out.println("SUCCESS: " + sql.substring(0, Math.min(80, sql.length())));
                } catch (Exception e) {
                    System.out.println("FAILED: " + sql.substring(0, Math.min(80, sql.length())));
                    System.out.println("  Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
