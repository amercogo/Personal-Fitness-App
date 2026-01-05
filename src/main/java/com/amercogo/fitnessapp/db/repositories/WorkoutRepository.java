package com.amercogo.fitnessapp.db.repositories;

import com.amercogo.fitnessapp.auth.SessionManager;
import com.amercogo.fitnessapp.db.DBConnection;
import com.amercogo.fitnessapp.model.Workout;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkoutRepository implements CrudRepository<Workout, String> {

    private String requireUserId() {
        String uid = SessionManager.getUserId();
        if (uid == null || uid.isBlank()) throw new IllegalStateException("Not logged in (missing userId).");
        return uid;
    }

    @Override
    public Workout save(Workout w) {
        String uid = requireUserId();

        String sql = """
                insert into public.workouts (user_id, name, workout_date, duration_minutes)
                values (?, ?, ?, ?)
                returning id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, uid, Types.OTHER);
            ps.setString(2, w.getName());
            ps.setDate(3, w.getDate() == null ? null : Date.valueOf(w.getDate()));
            ps.setInt(4, w.getDurationMinutes());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) w.setId(rs.getString("id"));
            }

            w.setUserId(uid);
            return w;

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri save workout", ex);
        }
    }

    @Override
    public Optional<Workout> findById(String id) {
        String uid = requireUserId();

        String sql = """
                select id, user_id, name, workout_date, duration_minutes
                  from public.workouts
                 where id = ? and user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id, Types.OTHER);
            ps.setObject(2, uid, Types.OTHER);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri findById workout", ex);
        }
    }

    @Override
    public List<Workout> findAll() {
        String uid = requireUserId();

        String sql = """
                select id, user_id, name, workout_date, duration_minutes
                  from public.workouts
                 where user_id = ?
                 order by workout_date desc nulls last
                """;

        List<Workout> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, uid, Types.OTHER);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri findAll workouts", ex);
        }
    }

    @Override
    public void update(Workout w) {
        String uid = requireUserId();

        String sql = """
                update public.workouts
                   set name = ?, workout_date = ?, duration_minutes = ?
                 where id = ? and user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, w.getName());
            ps.setDate(2, w.getDate() == null ? null : Date.valueOf(w.getDate()));
            ps.setInt(3, w.getDurationMinutes());
            ps.setObject(4, w.getId(), Types.OTHER);
            ps.setObject(5, uid, Types.OTHER);

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri update workout", ex);
        }
    }

    @Override
    public void deleteById(String id) {
        String uid = requireUserId();

        String sql = "delete from public.workouts where id = ? and user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id, Types.OTHER);
            ps.setObject(2, uid, Types.OTHER);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri deleteById workout", ex);
        }
    }

    private Workout mapRow(ResultSet rs) throws SQLException {
        Workout w = new Workout();
        w.setId(rs.getString("id"));
        w.setUserId(rs.getString("user_id"));
        w.setName(rs.getString("name"));

        Date d = rs.getDate("workout_date");
        w.setDate(d == null ? null : d.toLocalDate());

        w.setDurationMinutes(rs.getInt("duration_minutes"));
        return w;
    }
}
