import java.sql.*;

public class AddSubcategoryIdAndMore {
    public static void main(String[] a) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        Statement stmt = conn.createStatement();

        String[] adds = {
            "ALTER TABLE vendor_medicine ADD COLUMN IF NOT EXISTS subcategory_id BIGINT",
            "ALTER TABLE vendor_medicine ADD COLUMN IF NOT EXISTS country_of_origin VARCHAR(50)",
            "ALTER TABLE vendor_medicine ADD COLUMN IF NOT EXISTS medicine_type VARCHAR(50)",
            "ALTER TABLE vendor_medicine ADD COLUMN IF NOT EXISTS salt_composition VARCHAR(255)",
        };

        for (String sql : adds) {
            try {
                stmt.executeUpdate(sql);
                System.out.println("OK: " + sql.substring(0, Math.min(80, sql.length())));
            } catch (Exception e) {
                System.out.println("FAIL: " + e.getMessage());
            }
        }
        conn.close();
        System.out.println("Done!");
    }
}
