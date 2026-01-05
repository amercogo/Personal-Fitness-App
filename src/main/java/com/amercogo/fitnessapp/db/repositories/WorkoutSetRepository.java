package com.amercogo.fitnessapp.db.repositories;

import com.amercogo.fitnessapp.auth.SessionManager;
import com.amercogo.fitnessapp.db.DBConnection;
import com.amercogo.fitnessapp.model.WorkoutSet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkoutSetRepository implements CrudRepository<WorkoutSet, String> {

    private String requireUserId() {
        String uid = SessionManager.getUserId();
        if (uid == null || uid.isBlank()) throw new IllegalStateException("Not logged in (missing userId).");
        return uid;
    }

    @Override
    public WorkoutSet save(WorkoutSet s) {
        String uid = requireUserId();

        String sql = """
                insert into public.workout_sets (user_id, workout_id, exercise_id, set_no, reps, weight_kg)
                values (?, ?, ?, ?, ?, ?)
                returning id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, uid, Types.OTHER);
            ps.setObject(2, s.getWorkoutId(), Types.OTHER);
            ps.setObject(3, s.getExerciseId(), Types.OTHER);
            ps.setInt(4, s.getSetNo());
            ps.setInt(5, s.getReps());
            ps.setBigDecimal(6, java.math.BigDecimal.valueOf(s.getWeightKg()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) s.setId(rs.getString("id"));
            }

            return s;

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri save workout_set", ex);
        }
    }

    @Override
    public Optional<WorkoutSet> findById(String id) {
        String uid = requireUserId();

        String sql = """
                select id, workout_id, exercise_id, set_no, reps, weight_kg
                  from public.workout_sets
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
            throw new RuntimeException("Greška pri findById workout_set", ex);
        }
    }

    @Override
    public List<WorkoutSet> findAll() {
        String uid = requireUserId();

        String sql = """
                select id, workout_id, exercise_id, set_no, reps, weight_kg
                  from public.workout_sets
                 where user_id = ?
                 order by created_at desc
                """;

        List<WorkoutSet> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, uid, Types.OTHER);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri findAll workout_sets", ex);
        }
    }

    public List<WorkoutSet> findByWorkoutId(String workoutId) {
        String uid = requireUserId();

        String sql = """
                select id, workout_id, exercise_id, set_no, reps, weight_kg
                  from public.workout_sets
                 where workout_id = ? and user_id = ?
                 order by set_no asc, created_at asc
                """;

        List<WorkoutSet> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, workoutId, Types.OTHER);
            ps.setObject(2, uid, Types.OTHER);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri findByWorkoutId workout_sets", ex);
        }
    }

    @Override
    public void update(WorkoutSet s) {
        String uid = requireUserId();

        String sql = """
                update public.workout_sets
                   set exercise_id = ?, set_no = ?, reps = ?, weight_kg = ?
                 where id = ? and user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, s.getExerciseId(), Types.OTHER);
            ps.setInt(2, s.getSetNo());
            ps.setInt(3, s.getReps());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(s.getWeightKg()));
            ps.setObject(5, s.getId(), Types.OTHER);
            ps.setObject(6, uid, Types.OTHER);

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri update workout_set", ex);
        }
    }

    @Override
    public void deleteById(String id) {
        String uid = requireUserId();

        String sql = "delete from public.workout_sets where id = ? and user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id, Types.OTHER);
            ps.setObject(2, uid, Types.OTHER);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri deleteById workout_set", ex);
        }
    }

    private WorkoutSet mapRow(ResultSet rs) throws SQLException {
        WorkoutSet s = new WorkoutSet();
        s.setId(rs.getString("id"));
        s.setWorkoutId(rs.getString("workout_id"));
        s.setExerciseId(rs.getString("exercise_id"));
        s.setSetNo(rs.getInt("set_no"));
        s.setReps(rs.getInt("reps"));
        s.setWeightKg(rs.getBigDecimal("weight_kg").doubleValue());
        return s;
    }
}
