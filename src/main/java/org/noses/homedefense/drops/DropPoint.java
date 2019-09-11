package org.noses.homedefense.drops;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
class DropPoint {
    @PrimaryKeyColumn(name = "partition_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String partitionId;

    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private long id;

    @PrimaryKeyColumn(name = "lat", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private double lat;

    @PrimaryKeyColumn(name = "lon", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private double lon;
}
