package org.noses.homedefense.maps;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MapDTO {
    List<WayDTO> ways;
    List<NestDTO> nests;

    double north;
    double south;
    double east;
    double west;

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
    String highway;

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
    double lat;
    double lon;

    long id;

    int order;
}

@Data
@AllArgsConstructor
class NestDTO {
    double lat;
    double lon;

    long id;

    String type;
}

@Data
@AllArgsConstructor
class TowerDTO {
    double longitude;
    double latitude;

    String towerType;
}

