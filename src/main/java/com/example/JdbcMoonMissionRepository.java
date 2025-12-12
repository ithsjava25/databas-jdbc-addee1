package com.example;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMoonMissionRepository implements MoonMissionRepository {

    private final DataSource ds;

    public JdbcMoonMissionRepository(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public List<String> listSpacecraft() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT spacecraft FROM moon_mission";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("spacecraft"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Mission getMissionById(long id) {
        String sql = "SELECT mission_id, spacecraft FROM moon_mission WHERE mission_id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Mission(
                            rs.getLong("mission_id"),
                            rs.getString("spacecraft")
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countByYear(int year) {
        String sql = "SELECT COUNT(*) AS total FROM moon_mission WHERE YEAR(launch_date) = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
