package org.noses.homedefense.maps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("destination")
@Data
public class Destination {

    @PrimaryKey
    private DestinationKey key;

    private double lat;
    private double lon;

    private String name;
    private String description;

}


@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
class DestinationKey {
    @PrimaryKeyColumn(name = "partition_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String partitionId = "destinations";

    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private String id;
}