package org.noses.homedefense.users;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
@Data
public class AccountSession {

    @PrimaryKey
    private AccountSessionKey token;

    private String accountPartitionId;

    private String username;

    private String email;

    public static String getPartitionIdFromToken(String token) {
        return token.substring(token.length()-4);
    }
}


@Data
@PrimaryKeyClass
class AccountSessionKey {
    @PrimaryKeyColumn(name = "partition_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String partitionId;

    @PrimaryKeyColumn(name = "token", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private String token;
}