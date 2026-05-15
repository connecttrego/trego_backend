import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixRemainingColumns {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        String[] statements = {
            // orders.vendor_id is BIGINT in DB but entity Vendor.id is int -> fix Vendor.id to be int in DB
            "ALTER TABLE orders MODIFY COLUMN vendor_id INT",
        };

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                try {
                    stmt.executeUpdate(sql);
                    System.out.println("SUCCESS: " + sql);
                } catch (Exception e) {
                    System.out.println("FAILED: " + sql);
                    System.out.println("  Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
