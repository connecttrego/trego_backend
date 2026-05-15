import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FixAllDoubleColumns {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        // Check which columns need fixing and fix them
        List<String> alterStatements = new ArrayList<>();
        
        // order_items: amount, mrp, selling_price
        alterStatements.add("ALTER TABLE order_items MODIFY COLUMN amount DECIMAL(38,2), MODIFY COLUMN mrp DECIMAL(38,2), MODIFY COLUMN selling_price DECIMAL(38,2)");
        
        // orders: total_amount, discount
        alterStatements.add("ALTER TABLE orders MODIFY COLUMN total_amount DECIMAL(38,2), MODIFY COLUMN discount DECIMAL(38,2)");
        
        // vendor_informations (vendor): lat, lng
        alterStatements.add("ALTER TABLE vendor_informations MODIFY COLUMN lat DECIMAL(38,2), MODIFY COLUMN lng DECIMAL(38,2)");
        
        // vendor_medicine_price (stock): mrp, discount
        alterStatements.add("ALTER TABLE vendor_medicine_price MODIFY COLUMN mrp DECIMAL(38,2), MODIFY COLUMN discount DECIMAL(38,2)");
        
        // pre_orders: total_pay_amount
        alterStatements.add("ALTER TABLE pre_orders MODIFY COLUMN total_pay_amount DECIMAL(38,2)");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            for (String sql : alterStatements) {
                try {
                    stmt.executeUpdate(sql);
                    System.out.println("SUCCESS: " + sql.substring(0, Math.min(80, sql.length())));
                } catch (Exception e) {
                    System.out.println("FAILED: " + sql.substring(0, Math.min(80, sql.length())));
                    System.out.println("  Error: " + e.getMessage());
                }
            }
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
