package koeko.database_management;

import koeko.view.Utilities;

import java.sql.*;
import java.util.ArrayList;

public class DbUtils {
    static public ArrayList<String> getArrayStringWithThreeParams(String sql, String param1, String param2, String param3) {
        ArrayList<String> strings = new ArrayList<>();
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1,param1);
            pstmt.setString(2,param2);
            pstmt.setString(3,param3);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                strings.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strings;
    }

    static public ArrayList<String> getArrayStringWithOneParam(String sql, String param1) {
        ArrayList<String> strings = new ArrayList<>();
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1,param1);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                strings.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strings;
    }

    static public String getStringValueWithOneParam(String sql, String param1) {
        String returnValue = "";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1,param1);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                returnValue = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    static public void updateWithNoParam(String sql) {
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void updateWithOneParam(String sql, String param1) {
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1,param1);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void updateWithTwoParam(String sql, String param1, String param2) {
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1,param1);
            pstmt.setString(2,param2);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateWithThreeParam(String sql, String param1, String param2, String param3) {
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1,param1);
            pstmt.setString(2,param2);
            pstmt.setString(3,param3);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void updateWithFourParam(String sql, String param1, String param2, String param3, String param4) {
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1,param1);
            pstmt.setString(2,param2);
            pstmt.setString(3,param3);
            pstmt.setString(4,param4);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void updateWithFiveParam(String sql, String param1, String param2, String param3, String param4, String param5) {
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1,param1);
            pstmt.setString(2,param2);
            pstmt.setString(3,param3);
            pstmt.setString(4,param4);
            pstmt.setString(5,param5);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public Timestamp getModifDateAsTimestamp(ResultSet rs) throws SQLException {
        Timestamp modifDate;
        if (rs.getString("MODIF_DATE") != null) {
            try {
                modifDate = Timestamp.valueOf(rs.getString("MODIF_DATE"));
            } catch(IllegalArgumentException e) {
                modifDate = new Timestamp(0);
                System.out.println(rs.getString("MODIF_DATE"));
                e.printStackTrace();
            }
        } else {
            modifDate = new Timestamp(0);
        }
        return modifDate;
    }

    static public String getHashCode(String sequence) {
        char[] seq = sequence.toCharArray();
        int hash = 5381;
        for (int i = 0; i < sequence.length(); i++) {
            hash = (hash % 60606060) * 33 + (int)seq[i];
        }

        return String.valueOf(hash);
    }
}
