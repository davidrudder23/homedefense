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

    private int order;

    private long node;

    public WayNode() {
        wayNodeKey = new WayNodeKey();
        wayNodeKey.setPartitionId(1);
    }

}

@PrimaryKeyClass
@Data
class WayNodeKey {
    @PrimaryKeyColumn(name = "partition_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private int partitionId;

    @PrimaryKeyColumn(name = "lat", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private float lat;

    @PrimaryKeyColumn(name = "lon", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private float lon;

    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private long id;

    @PrimaryKeyColumn(name = "way", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private long way;


    public void setLon(float lon) {
        this.lon = lon;
        partitionId = Math.round((lon+180)/10);
    }

}
