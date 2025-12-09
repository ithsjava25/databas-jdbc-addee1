package com.example;

import javax.sql.DataSource;
import java.sql.*;

public class JdbcAccountRepository implements AccountRepository {

    private final DataSource ds;

    public JdbcAccountRepository(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public boolean login(String username, String password) {
        String sql = "SELECT 1 FROM account WHERE name = ? AND password = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long create(String first, String last, String ssn, String password) {
        String sql = "INSERT INTO account(first_name, last_name, ssn, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, ssn);
            ps.setString(4, password);

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getLong(1);
            }
            throw new RuntimeException("No ID returned!");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE account SET password = ? WHERE user_id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM account WHERE user_id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(int userId) {
        String sql = "SELECT 1 FROM account WHERE user_id = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
