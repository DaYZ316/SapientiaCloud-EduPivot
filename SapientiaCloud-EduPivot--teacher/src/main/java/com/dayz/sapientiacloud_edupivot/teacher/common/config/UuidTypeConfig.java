package com.dayz.sapientiacloud_edupivot.teacher.common.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@MappedTypes(UUID.class)
@MappedJdbcTypes(JdbcType.BINARY)
public class UuidTypeConfig extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        byte[] bytes = new byte[16];
        long msb = parameter.getMostSignificantBits();
        long lsb = parameter.getLeastSignificantBits();
        for (int j = 0; j < 8; j++) {
            bytes[j] = (byte) (msb >>> (8 * (7 - j)));
            bytes[j + 8] = (byte) (lsb >>> (8 * (7 - j)));
        }
        ps.setBytes(i, bytes);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toUUID(rs.getBytes(columnName));
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toUUID(rs.getBytes(columnIndex));
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toUUID(cs.getBytes(columnIndex));
    }

    private UUID toUUID(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            return null;
        }
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
            lsb = (lsb << 8) | (bytes[i + 8] & 0xff);
        }
        return new UUID(msb, lsb);
    }
}