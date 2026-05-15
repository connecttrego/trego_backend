import java.sql.*;

public class FinalRevertAll {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        Statement stmt = conn.createStatement();

        System.out.println("Dropping FKs...");
        tryExec(stmt, "ALTER TABLE vendor_application_status DROP FOREIGN KEY fk_status_applicant");
        tryExec(stmt, "ALTER TABLE vendor_medicine_price DROP FOREIGN KEY FKad4jss13qgfagkp7blyjggf9w");
        tryExec(stmt, "ALTER TABLE vendor_personal_details DROP FOREIGN KEY fk_personal_applicant");
        // orders FK was already dropped or failed to be re-added

        System.out.println("Modifying column types to BIGINT...");
        tryExec(stmt, "ALTER TABLE vendor_informations MODIFY COLUMN vendor_user_id BIGINT");
        tryExec(stmt, "ALTER TABLE vendor_application_status MODIFY COLUMN applicant_id BIGINT");
        tryExec(stmt, "ALTER TABLE vendor_medicine_price MODIFY COLUMN vendor_id BIGINT");
        tryExec(stmt, "ALTER TABLE vendor_personal_details MODIFY COLUMN applicant_id BIGINT");
        tryExec(stmt, "ALTER TABLE orders MODIFY COLUMN vendor_id BIGINT");

        System.out.println("Re-adding FKs...");
        tryExec(stmt, "ALTER TABLE vendor_application_status ADD CONSTRAINT fk_status_applicant FOREIGN KEY (applicant_id) REFERENCES vendor_informations(vendor_user_id)");
        tryExec(stmt, "ALTER TABLE vendor_medicine_price ADD CONSTRAINT FKad4jss13qgfagkp7blyjggf9w FOREIGN KEY (vendor_id) REFERENCES vendor_informations(vendor_user_id)");
        tryExec(stmt, "ALTER TABLE vendor_personal_details ADD CONSTRAINT fk_personal_applicant FOREIGN KEY (applicant_id) REFERENCES vendor_informations(vendor_user_id)");
        tryExec(stmt, "ALTER TABLE orders ADD CONSTRAINT FKb1443sk0bxprtkqree3o2qk90 FOREIGN KEY (vendor_id) REFERENCES vendor_informations(vendor_user_id)");

        conn.close();
        System.out.println("Full Revert Complete!");
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
