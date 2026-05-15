import java.sql.*;

public class FinalizeRevert {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // 1. Change vendor_user_id to BIGINT to match orders.vendor_id (which is now BIGINT)
            // We must drop FKs pointing to it first if any
            tryExec(stmt, "ALTER TABLE vendor_informations MODIFY COLUMN vendor_user_id BIGINT");
            
            // 2. Re-add the FK in orders
            tryExec(stmt, "ALTER TABLE orders ADD CONSTRAINT FKb1443sk0bxprtkqree3o2qk90 FOREIGN KEY (vendor_id) REFERENCES vendor_informations(vendor_user_id)");

            System.out.println("Finalize Revert done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void tryExec(Statement stmt, String sql) {
        try {
            stmt.executeUpdate(sql);
            System.out.println("SUCCESS: " + sql);
        } catch (Exception e) {
            System.out.println("FAILED : " + sql);
            System.out.println("  Error: " + e.getMessage());
        }
    }
}
