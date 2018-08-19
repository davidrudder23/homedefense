package org.noses.homedefense.maps;

import com.datastax.driver.core.DataType;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

@Table("node")
@Data
public class Node {

    @PrimaryKey
    private Point point;

    public Node() {
        point = new Point();
        point.setPartitionId(1);
    }

}

@Data
@PrimaryKeyClass
class Point {
    @PrimaryKeyColumn(name = "partition_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private int partitionId;

    @PrimaryKeyColumn(name = "lat", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private float lat;

    @PrimaryKeyColumn(name = "lon", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private float lon;

    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private long id;


    public void setLon(float lon) {
        this.lon = lon;
        partitionId = Math.round((lon+180)/10);
    }
}