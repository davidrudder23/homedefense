package org.noses.homedefense.maps;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Table
@Data
public class Way {

    @PrimaryKey
    private long id;

    private String name;

    private int lanes;

    private int maxSpeed;

    private boolean oneWay;

    List<Long> nodes;

    public void addNode(Long nodeId) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        nodes.add(nodeId);
    }

}
