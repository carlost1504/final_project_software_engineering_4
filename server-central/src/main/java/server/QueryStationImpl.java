package server;

import com.zeroc.Ice.Current;
import common.QueryStation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueryStationImpl implements QueryStation {

    @Override
    public String query(String document, Current current) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT is_enabled, has_voted, assigned_station_id FROM voters WHERE document = ?"
            );
            stmt.setString(1, document);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return " Documento no registrado.";
            }

            boolean isEnabled = rs.getBoolean("is_enabled");
            boolean hasVoted = rs.getBoolean("has_voted");
            int assignedStationId = rs.getInt("assigned_station_id");

            if (!isEnabled) return " Votante no habilitado.";
            if (hasVoted) return " Ya ha votado.";
            return " Habilitado. Estación asignada: " + assignedStationId;

        } catch (Exception e) {
            System.err.println("Error en consulta de votante: " + e.getMessage());
            return " Error interno al consultar el estado del votante.";
        }
    }
}
