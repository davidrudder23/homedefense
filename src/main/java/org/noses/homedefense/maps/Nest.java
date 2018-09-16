package org.noses.homedefense.maps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("nest")
@Data
public class Nest {

    @PrimaryKey
    private NestPoint point;

    private String type;

    public Nest() {
        point = new NestPoint();
        type = "standard";
    }

    public String getPartitionId() {
        return Math.round((getPoint().getLon() + 180) * 200) +
                "x" +
                Math.round((getPoint().getLat() + 180) * 200);
    }

    public static String getPartitionId(float lat, float lon) {
        return Math.round((lon + 180) * 200) +
                "x" +
                Math.round((lat + 180) * 200);
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
class NestPoint {
    @PrimaryKeyColumn(name = "partition_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String partitionId;

    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private long id;

    @PrimaryKeyColumn(name = "lat", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private float lat;

    @PrimaryKeyColumn(name = "lon", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private float lon;
}
