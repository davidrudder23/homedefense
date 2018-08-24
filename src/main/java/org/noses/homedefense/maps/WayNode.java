package org.noses.homedefense.maps;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
@Data
public class WayNode {
    @PrimaryKey
    private WayNodeKey wayNodeKey;

    private long node;

    private int orderNum;

    public WayNode() {
        wayNodeKey = new WayNodeKey();
        wayNodeKey.setPartitionId("0x0");
    }

    private String name;

    private int lanes;

    private int maxSpeed;

    private boolean oneWay;

    private String highway;

    private long way;
}

@PrimaryKeyClass
@Data
class WayNodeKey {
    @PrimaryKeyColumn(name = "partition_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String partitionId;

    @PrimaryKeyColumn(name = "lat", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private float lat;

    @PrimaryKeyColumn(name = "lon", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private float lon;

    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private long id;

}
