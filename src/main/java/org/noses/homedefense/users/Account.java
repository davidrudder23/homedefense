package org.noses.homedefense.users;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigInteger;

@Table
@Data
public class Account
{
    @PrimaryKey
    private AccountPrimaryKey accountPrimaryKey;

    private BigInteger score;

    private float homeLongitude;

    private float homeLatitude;

    private String hashedPassword;

    public static String getPartitionIdByUsername(String username) {
        return username.substring(username.length()-4);
    }

}

@PrimaryKeyClass
@Data
class AccountPrimaryKey {
    @PrimaryKeyColumn(name = "partition_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String partitionId;

    @PrimaryKeyColumn(name = "username", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private String username;

    @PrimaryKeyColumn(name = "email", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private String email;

}