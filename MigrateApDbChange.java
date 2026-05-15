import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MigrateApDbChange {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // 1. Insert default vendor into vendor_informations
            String vendorSql = "INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, address) VALUES (1, 'Trego Default Vendor', 'Default Address')";
            stmt.executeUpdate(vendorSql);

            // 2. Insert medicines into vendor_medicine
            String medicineSql = "INSERT IGNORE INTO vendor_medicine (" +
                    "vendor_medicine_id, name, manufacture, manufacturer_address, country_of_origin, " +
                    "medicine_type, description, introduction, how_it_works, safety_advise, " +
                    "if_miss, packing, prescription_required, storage, use_of, " +
                    "common_side_effect, alcohol_interaction, driving_interaction, kidney_interaction, " +
                    "lactation_interaction, liver_interaction, pregnancy_interaction, question_answers, " +
                    "salt_composition) " +
                    "SELECT " +
                    "medicine_id, name, manufacture, manufacturer_address, country_of_origin, " +
                    "medicine_type, description, introduction, how_works, safety_advise, " +
                    "if_miss, packaging, prescription_required, storage, use_of, " +
                    "common_side_effect, alcohol_interaction, driving_interaction, kidney_interaction, " +
                    "lactation_interaction, liver_interaction, pregnancy_interaction, question_answers, " +
                    "salt_composition " +
                    "FROM medicine_master_db_table";
            int medRows = stmt.executeUpdate(medicineSql);
            System.out.println("Migrated " + medRows + " rows into vendor_medicine");

            // 3. Insert stocks into vendor_medicine_price
            String stockSql = "INSERT IGNORE INTO vendor_medicine_price (discount, mrp, quantity, vendor_medicine_id, vendor_id, expiry_date) " +
                    "SELECT COALESCE(offer_percent, 0), COALESCE(mrp, 0), COALESCE(quantity, 10), medicine_id, 1, expiry_date " +
                    "FROM medicine_master_db_table";
            int stockRows = stmt.executeUpdate(stockSql);
            System.out.println("Migrated " + stockRows + " rows into vendor_medicine_price");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
