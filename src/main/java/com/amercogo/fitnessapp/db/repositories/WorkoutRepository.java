package com.amercogo.fitnessapp.db.repositories;

import com.amercogo.fitnessapp.db.DBConnection;
import com.amercogo.fitnessapp.model.Workout;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkoutRepository implements CrudRepository<Workout, String> {

    @Override
    public Workout save(Workout w) {
        String sql = """
            insert into public.workouts (user_id, name, workout_date, duration_minutes)
            values (?, ?, ?, ?)
            returning id
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (w.getUserId() == null || w.getUserId().isBlank()) {
                ps.setNull(1, Types.OTHER); // uuid
            } else {
                ps.setObject(1, w.getUserId(), Types.OTHER);
            }

            ps.setString(2, w.getName());
            ps.setDate(3, Date.valueOf(w.getDate()));
            ps.setInt(4, w.getDurationMinutes());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    w.setId(rs.getString("id"));
                }
            }
            return w;

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri save workout", e);
        }
    }

    @Override
    public Optional<Workout> findById(String id) {
        String sql = "select id, user_id, name, workout_date, duration_minutes from public.workouts where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id, Types.OTHER);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri findById workout", e);
        }
    }

    @Override
    public List<Workout> findAll() {
        String sql = "select id, user_id, name, workout_date, duration_minutes from public.workouts order by workout_date desc";
        List<Workout> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri findAll workouts", e);
        }
    }

    @Override
    public void update(Workout w) {
        String sql = """
            update public.workouts
               set user_id = ?, name = ?, workout_date = ?, duration_minutes = ?
             where id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (w.getUserId() == null || w.getUserId().isBlank()) {
                ps.setNull(1, Types.OTHER);
            } else {
                ps.setObject(1, w.getUserId(), Types.OTHER);
            }

            ps.setString(2, w.getName());
            ps.setDate(3, Date.valueOf(w.getDate()));
            ps.setInt(4, w.getDurationMinutes());
            ps.setObject(5, w.getId(), Types.OTHER);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri update workout", e);
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "delete from public.workouts where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id, Types.OTHER);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri deleteById workout", e);
        }
    }

    private Workout mapRow(ResultSet rs) throws SQLException {
        Workout w = new Workout();
        w.setId(rs.getString("id"));

        Object uid = rs.getObject("user_id");
        w.setUserId(uid == null ? null : uid.toString());

        w.setName(rs.getString("name"));

        Date d = rs.getDate("workout_date");
        w.setDate(d == null ? null : d.toLocalDate());

        w.setDurationMinutes(rs.getInt("duration_minutes"));
        return w;
    }
}
