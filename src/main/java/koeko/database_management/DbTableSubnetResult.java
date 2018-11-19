package koeko.database_management;

import koeko.Networking.SubNet;
import koeko.Networking.SubnetResult;
import koeko.view.Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DbTableSubnetResult {
    static private String KEY_TABLE_SUBNETRESULT = "subnet_result";
    static private String KEY_DEVICE_ID = "device_id";
    static private String KEY_DEVICE_ROLE = "device_role";
    static private String KEY_DEVICE_MODEL = "device_model";
    static private String KEY_SDK_LEVEL = "sdk_level";
    static private String KEY_SUCCESS = "success";

    static public void createTableSubnetResults() {
        String sql = "CREATE TABLE IF NOT EXISTS " + KEY_TABLE_SUBNETRESULT +
                " (ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_DEVICE_ID + " TEXT    NOT NULL, " +
                KEY_DEVICE_ROLE + " INTEGER   NOT NULL, " +
                KEY_DEVICE_MODEL + " TEXT, " +
                KEY_SDK_LEVEL + " INTEGER, " +
                KEY_SUCCESS + " INTEGER NOT NULL, " +
                " UNIQUE (ID)) ";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public void insertSubnetResult(SubnetResult subnetResult) {
        String sql = "INSERT OR REPLACE INTO " + KEY_TABLE_SUBNETRESULT + " (" + KEY_DEVICE_ID + "," +
                KEY_DEVICE_ROLE + "," + KEY_DEVICE_MODEL + "," + KEY_SDK_LEVEL + "," + KEY_SUCCESS + ") VALUES(?,?,?,?,?)";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, subnetResult.getDeviceId());
            stmt.setInt(2, subnetResult.getDeviceRole());
            stmt.setString(3, subnetResult.getDeviceModel());
            stmt.setInt(4, subnetResult.getSdkLevel());
            stmt.setInt(5, subnetResult.getSuccess());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public ArrayList<SubnetResult> getSubnetResultForId(String deviceId, Integer deviceRole) {
        ArrayList<SubnetResult> subnetResults = new ArrayList<>();
        String sql = "SELECT * FROM " + KEY_TABLE_SUBNETRESULT + " WHERE " + KEY_DEVICE_ID + "=?" + " AND " + KEY_DEVICE_ROLE + "=?";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, deviceId);
            stmt.setInt(2, deviceRole);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SubnetResult subnetResult = new SubnetResult();
                subnetResult.setDeviceId(deviceId);
                subnetResult.setDeviceModel(rs.getString(KEY_DEVICE_MODEL));
                subnetResult.setDeviceRole(rs.getInt(KEY_DEVICE_ROLE));
                subnetResult.setSdkLevel(rs.getInt(KEY_SDK_LEVEL));
                subnetResult.setSuccess(rs.getInt(KEY_SUCCESS));
                subnetResults.add(subnetResult);
            }
        } catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return subnetResults;
    }
}
