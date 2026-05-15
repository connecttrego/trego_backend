import java.sql.*;

public class RevertWithNoChecks {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        Statement stmt = conn.createStatement();

        System.out.println("Disabling FK checks...");
        stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

        System.out.println("Reverting column types to original BIGINT and DOUBLE...");
        
        // IDs
        tryExec(stmt, "ALTER TABLE vendor_informations MODIFY COLUMN vendor_user_id BIGINT");
        tryExec(stmt, "ALTER TABLE vendor_application_status MODIFY COLUMN applicant_id BIGINT");
        tryExec(stmt, "ALTER TABLE vendor_medicine_price MODIFY COLUMN vendor_id BIGINT");
        tryExec(stmt, "ALTER TABLE vendor_personal_details MODIFY COLUMN applicant_id BIGINT");
        tryExec(stmt, "ALTER TABLE orders MODIFY COLUMN vendor_id BIGINT");
        tryExec(stmt, "ALTER TABLE pre_orders MODIFY COLUMN selected_vendor_id BIGINT");

        // Decimals to Double
        tryExec(stmt, "ALTER TABLE order_items MODIFY COLUMN amount DOUBLE, MODIFY COLUMN mrp DOUBLE, MODIFY COLUMN selling_price DOUBLE");
        tryExec(stmt, "ALTER TABLE orders MODIFY COLUMN total_amount DOUBLE, MODIFY COLUMN discount DOUBLE");
        tryExec(stmt, "ALTER TABLE vendor_informations MODIFY COLUMN lat DOUBLE, MODIFY COLUMN lng DOUBLE");
        tryExec(stmt, "ALTER TABLE vendor_medicine_price MODIFY COLUMN mrp DOUBLE, MODIFY COLUMN discount DOUBLE, MODIFY COLUMN selling_price DOUBLE");
        tryExec(stmt, "ALTER TABLE pre_orders MODIFY COLUMN total_pay_amount DOUBLE");

        System.out.println("Enabling FK checks...");
        stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

        conn.close();
        System.out.println("Final Revert with NO_CHECKS complete!");
    }

    static void tryExec(Statement stmt, String sql) {
        try {
            stmt.executeUpdate(sql);
            System.out.println("SUCCESS: " + sql);
        } catch (Exception e) {
            System.out.println("FAILED : " + sql + " - " + e.getMessage());
        }
    }
}
