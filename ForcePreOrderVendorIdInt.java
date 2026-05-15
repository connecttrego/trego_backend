import java.sql.*;

public class ForcePreOrderVendorIdInt {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        Statement stmt = conn.createStatement();
        
        System.out.println("Forcing pre_orders.selected_vendor_id to INT...");
        stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        try {
            stmt.executeUpdate("ALTER TABLE pre_orders MODIFY COLUMN selected_vendor_id INT");
            System.out.println("SUCCESS: pre_orders.selected_vendor_id is now INT");
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }
        stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        conn.close();
    }
}
