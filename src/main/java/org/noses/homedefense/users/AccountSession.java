package org.noses.homedefense.users;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.util.StringUtils;

@Table
@Data
public class AccountSession {

    public AccountSession() {
        authenticationToken = new AccountSessionKey();
    }
    @PrimaryKey
    private AccountSessionKey authenticationToken;

    private String accountPartitionId;

    private String username;

    private String email;

    public static String getPartitionIdFromToken(String token) {
        if (StringUtils.isEmpty(token) || token.length() < 4) {
            return null;
        }
        return token.substring(token.length()-4);
    }
}


@Data
@PrimaryKeyClass
class AccountSessionKey {
    @PrimaryKeyColumn(name = "partition_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String partitionId;

    @PrimaryKeyColumn(name = "authenticationtoken", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private String authenticationToken;
}