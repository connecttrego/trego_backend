import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class FixAllMismatches {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // pre_orders.selected_vendor_id: BIGINT -> INT
            // Entity has Integer selectedVendorId -> expects integer (INT)
            tryExec(stmt, "ALTER TABLE pre_orders MODIFY COLUMN selected_vendor_id INT");

            // Check order_items columns
            tryExec(stmt, "ALTER TABLE order_items MODIFY COLUMN created_at DATETIME(6)");
            tryExec(stmt, "ALTER TABLE order_items MODIFY COLUMN updated_at DATETIME(6)");

            // Check if vendor_medicine_price has selling_price column
            ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM vendor_medicine_price LIKE 'selling_price'");
            if (rs.next()) {
                tryExec(stmt, "ALTER TABLE vendor_medicine_price MODIFY COLUMN selling_price DECIMAL(38,2)");
            }

            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void tryExec(Statement stmt, String sql) {
        try {
            stmt.executeUpdate(sql);
            System.out.println("SUCCESS: " + sql.substring(0, Math.min(90, sql.length())));
        } catch (Exception e) {
            System.out.println("FAILED : " + sql.substring(0, Math.min(90, sql.length())));
            System.out.println("  Error: " + e.getMessage());
        }
    }
}
