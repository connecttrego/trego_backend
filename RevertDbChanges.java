import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class RevertDbChanges {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("Reverting manual changes...");

            // 1. Revert Decimal to Double
            tryExec(stmt, "ALTER TABLE order_items MODIFY COLUMN amount DOUBLE, MODIFY COLUMN mrp DOUBLE, MODIFY COLUMN selling_price DOUBLE");
            tryExec(stmt, "ALTER TABLE orders MODIFY COLUMN total_amount DOUBLE, MODIFY COLUMN discount DOUBLE");
            tryExec(stmt, "ALTER TABLE vendor_informations MODIFY COLUMN lat DOUBLE, MODIFY COLUMN lng DOUBLE");
            tryExec(stmt, "ALTER TABLE vendor_medicine_price MODIFY COLUMN mrp DOUBLE, MODIFY COLUMN discount DOUBLE, MODIFY COLUMN selling_price DOUBLE");
            tryExec(stmt, "ALTER TABLE pre_orders MODIFY COLUMN total_pay_amount DOUBLE");

            // 2. Revert IDs to BIGINT (Handling Foreign Keys)
            tryExec(stmt, "ALTER TABLE orders DROP FOREIGN KEY FKb1443sk0bxprtkqree3o2qk90");
            tryExec(stmt, "ALTER TABLE orders MODIFY COLUMN vendor_id BIGINT");
            tryExec(stmt, "ALTER TABLE orders ADD CONSTRAINT FKb1443sk0bxprtkqree3o2qk90 FOREIGN KEY (vendor_id) REFERENCES vendor_informations(vendor_user_id)");
            
            tryExec(stmt, "ALTER TABLE pre_orders MODIFY COLUMN selected_vendor_id BIGINT");

            // 3. Drop Added Columns from vendor_medicine
            String[] addedCols = {
                "alcohol_interaction", "pregnancy_interaction", "lactation_interaction", "driving_interaction",
                "kidney_interaction", "liver_interaction", "manufacturer_address", "description",
                "introduction", "how_it_works", "safety_advise", "if_miss", "common_side_effect",
                "question_answers", "use_of", "packing", "packaging_type", "photo1", "photo2", "photo3", "photo4",
                "subcategory_id", "country_of_origin", "medicine_type", "salt_composition"
            };
            for (String col : addedCols) {
                tryExec(stmt, "ALTER TABLE vendor_medicine DROP COLUMN " + col);
            }

            // 4. Revert Datetime(6) to Datetime (approximate revert)
            String[] tablesWithDate = {"address", "app_users", "attachments", "order_items", "orders", "pre_orders", "prescription_records"};
            for (String table : tablesWithDate) {
                tryExec(stmt, "ALTER TABLE " + table + " MODIFY COLUMN created_at DATETIME");
                tryExec(stmt, "ALTER TABLE " + table + " MODIFY COLUMN updated_at DATETIME");
                if (table.equals("app_users")) tryExec(stmt, "ALTER TABLE app_users MODIFY COLUMN email_verified_at DATETIME");
                if (table.equals("pre_orders")) tryExec(stmt, "ALTER TABLE pre_orders MODIFY COLUMN modified_at DATETIME");
            }

            System.out.println("Revert process finished!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void tryExec(Statement stmt, String sql) {
        try {
            stmt.executeUpdate(sql);
            System.out.println("SUCCESS: " + sql.substring(0, Math.min(80, sql.length())));
        } catch (Exception e) {
            System.out.println("FAILED : " + sql.substring(0, Math.min(80, sql.length())));
            System.out.println("  Error: " + e.getMessage());
        }
    }
}
