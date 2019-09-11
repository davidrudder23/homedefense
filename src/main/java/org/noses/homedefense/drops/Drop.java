package org.noses.homedefense.drops;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("drops")
@Data
public class Drop {

    @PrimaryKey
    private DropPoint point;

    private String className;

    private String json;

    private String accountId;

    public Drop() {
        point = new DropPoint();
    }

    public void setPartitionId() {
        point.setPartitionId(getPartitionId());
    }

    public String getPartitionId() {
        return Math.round((getPoint().getLon() + 180) * 200) +
                "x" +
                Math.round((getPoint().getLat() + 180) * 200);
    }

    public static String getPartitionId(double lat, double lon) {
        return Math.round((lon + 180) * 200) +
                "x" +
                Math.round((lat + 180) * 200);
    }

}

