import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class AddMissingColumns {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        // All columns that Medicine entity expects in vendor_medicine table
        // These match field names with Hibernate snake_case convention
        String[] columns = {
            "alcohol_interaction VARCHAR(255)",
            "pregnancy_interaction VARCHAR(255)",
            "lactation_interaction VARCHAR(255)",
            "driving_interaction VARCHAR(255)",
            "kidney_interaction VARCHAR(255)",
            "liver_interaction VARCHAR(255)",
            "manufacturer_address VARCHAR(255)",
            "description LONGTEXT",
            "introduction LONGTEXT",
            "how_it_works LONGTEXT",
            "safety_advise LONGTEXT",
            "if_miss LONGTEXT",
            "common_side_effect LONGTEXT",
            "question_answers LONGTEXT",
            "use_of VARCHAR(255)",
            "packing VARCHAR(255)",
            "packaging_type VARCHAR(255)",
            "prescription_required VARCHAR(50)",
            "storage VARCHAR(255)",
            "photo1 VARCHAR(255)",
            "photo2 VARCHAR(255)",
            "photo3 VARCHAR(255)",
            "photo4 VARCHAR(255)"
        };

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            for (String colDef : columns) {
                String colName = colDef.split(" ")[0];
                // Check if column already exists
                ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM vendor_medicine LIKE '" + colName + "'");
                if (!rs.next()) {
                    try {
                        stmt.executeUpdate("ALTER TABLE vendor_medicine ADD COLUMN " + colDef);
                        System.out.println("ADDED  : " + colName);
                    } catch (Exception e) {
                        System.out.println("FAILED : " + colName + " - " + e.getMessage());
                    }
                } else {
                    System.out.println("EXISTS : " + colName);
                }
            }
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
