package server.impl;

import com.zeroc.Ice.Current;
import common.QueryStation;
import server.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueryStationImpl implements QueryStation {

    @Override
    public String query(String document, Current current) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT v.is_enabled, v.has_voted, s.station_id, s.location " +
                            "FROM voters v " +
                            "JOIN stations s ON v.assigned_station_id = s.station_id " +
                            "WHERE v.document = ?"
            );
            stmt.setString(1, document);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return "Documento no registrado.";
            }

            boolean isEnabled = rs.getBoolean("is_enabled");
            boolean hasVoted = rs.getBoolean("has_voted");
            int stationId = rs.getInt("station_id");
            String location = rs.getString("location");

            if (!isEnabled) return "Votante no habilitado.";
            if (hasVoted) return "Ya ha votado.";

            return String.format("Habilitado para votar.\n Mesa: %d\n Ubicación: %s", stationId, location);

        } catch (Exception e) {
            System.err.println("Error en consulta de votante: " + e.getMessage());
            return "Error interno al consultar el estado del votante.";
        }
    }
}
