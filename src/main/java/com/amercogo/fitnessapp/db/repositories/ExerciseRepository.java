package com.amercogo.fitnessapp.db.repositories;

import com.amercogo.fitnessapp.db.DBConnection;
import com.amercogo.fitnessapp.model.Exercise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExerciseRepository implements CrudRepository<Exercise, String> {

    @Override
    public Exercise save(Exercise e) {
        String sql = """
                insert into public.exercises (name, muscle_group)
                values (?, ?)
                returning id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getName());
            if (e.getMuscleGroup() == null || e.getMuscleGroup().isBlank()) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, e.getMuscleGroup());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) e.setId(rs.getString("id"));
            }
            return e;

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri save exercise", ex);
        }
    }

    @Override
    public Optional<Exercise> findById(String id) {
        String sql = "select id, name, muscle_group from public.exercises where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id, Types.OTHER);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri findById exercise", ex);
        }
    }

    public Optional<Exercise> findByName(String name) {
        String sql = "select id, name, muscle_group from public.exercises where lower(name) = lower(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri findByName exercise", ex);
        }
    }

    @Override
    public List<Exercise> findAll() {
        String sql = "select id, name, muscle_group from public.exercises order by name asc";
        List<Exercise> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri findAll exercises", ex);
        }
    }

    @Override
    public void update(Exercise e) {
        String sql = """
                update public.exercises
                   set name = ?, muscle_group = ?
                 where id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getName());
            if (e.getMuscleGroup() == null || e.getMuscleGroup().isBlank()) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, e.getMuscleGroup());
            }
            ps.setObject(3, e.getId(), Types.OTHER);

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri update exercise", ex);
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "delete from public.exercises where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id, Types.OTHER);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Greška pri deleteById exercise", ex);
        }
    }

    private Exercise mapRow(ResultSet rs) throws SQLException {
        Exercise e = new Exercise();
        e.setId(rs.getString("id"));
        e.setName(rs.getString("name"));
        e.setMuscleGroup(rs.getString("muscle_group"));
        return e;
    }
}
