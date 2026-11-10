package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import com.baseplus.modules.auth.service.RefreshTokenHasher;

public class V14__hash_refresh_tokens extends BaseJavaMigration {

    private static final int BATCH_SIZE = 500;

    @Override
    public void migrate(Context context) throws Exception {
        var connection = context.getConnection();
        try (var statement = connection.createStatement()) {
            statement.execute("alter table refresh_tokens add column token_hash varchar(64)");
        }

        long lastId = 0;
        while (true) {
            List<TokenRow> batch = loadBatch(connection, lastId);
            if (batch.isEmpty()) {
                break;
            }
            updateBatch(connection, batch);
            lastId = batch.get(batch.size() - 1).id();
        }

        try (var statement = connection.createStatement()) {
            statement.execute("alter table refresh_tokens alter column token_hash set not null");
            statement.execute("alter table refresh_tokens drop constraint refresh_tokens_token_key");
            statement.execute("alter table refresh_tokens drop column token");
            statement.execute("create unique index ux_refresh_tokens_token_hash on refresh_tokens (token_hash)");
        }
    }

    private List<TokenRow> loadBatch(java.sql.Connection connection, long lastId) throws SQLException {
        List<TokenRow> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "select id, token from refresh_tokens where id > ? order by id limit ?")) {
            statement.setLong(1, lastId);
            statement.setInt(2, BATCH_SIZE);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String token = resultSet.getString("token");
                    if (token == null) {
                        throw new SQLException("Refresh token nulo encontrado durante a migração.");
                    }
                    rows.add(new TokenRow(resultSet.getLong("id"), token));
                }
            }
        }
        return rows;
    }

    private void updateBatch(java.sql.Connection connection, List<TokenRow> rows) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "update refresh_tokens set token_hash = ? where id = ?")) {
            for (TokenRow row : rows) {
                statement.setString(1, RefreshTokenHasher.sha256Hex(row.token()));
                statement.setLong(2, row.id());
                statement.addBatch();
            }
            int[] updated = statement.executeBatch();
            if (updated.length != rows.size()) {
                throw new SQLException("A migração não atualizou todos os refresh tokens.");
            }
            for (int count : updated) {
                if (count == 0 || count == Statement.EXECUTE_FAILED) {
                    throw new SQLException("A migração não atualizou todos os refresh tokens.");
                }
            }
        }
    }

    private record TokenRow(long id, String token) {
    }
}
