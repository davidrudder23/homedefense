package org.noses.homedefense.maps;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MapDTO {
    List<WayDTO> ways;
    int width;
    int height;

    public MapDTO() {
        ways = new ArrayList<>();
    }

    public void addWay(WayDTO way) {
        this.ways.add(way);
    }
}

@Data
class WayDTO {
    String name;
    int lanes;
    int maxSpeed;

    List<NodeDTO> nodes;

    public WayDTO() {
        this.nodes = new ArrayList<>();
    }

    public void addNode(NodeDTO node) {
        nodes.add(node);
    }
}

@Data
@AllArgsConstructor
class NodeDTO {
    int x;
    int y;
}

