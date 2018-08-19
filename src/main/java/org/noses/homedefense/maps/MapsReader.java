package org.noses.homedefense.maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MapsReader {

    @Autowired
    MapsRepository mapsRepository;

    public MapDTO readMap(int width,
                        int height,
                        float north,
                        float west,
                        float south,
                        float east) {
        List<Node> nodes = mapsRepository.getNodes(north, west, south, east);
        List<WayNode> wayNodes = mapsRepository.getWayNodes(nodes.stream().map(n -> n.getPoint().getId()).collect(Collectors.toList()));
        List<Way> ways = mapsRepository.getWays(wayNodes.stream().map(wn -> wn.getWayNodeKey().getWay()).collect(Collectors.toList()));
        // Now, get all the nodes for the ways
        List<Long> nodeIds = new ArrayList<>();
        for (Way way: ways) {
            for (WayNode wayNode: wayNodes) {
                if (wayNode.getWayNodeKey().getWay() == way.getId()) {
                    nodeIds.add(wayNode.getWayNodeKey().getNode());
                    continue;
                }
            }
        }
        nodes = mapsRepository.getNodesForWays(nodeIds);

        MapDTO mapDTO = new MapDTO();
        mapDTO.setWidth(width);
        mapDTO.setHeight(height);

        float pixelHeight = Math.abs(north - south) / height;
        float pixelWidth = Math.abs(east - west) / width;

        for (Way way : ways) {
            WayDTO wayDTO = new WayDTO();
            wayDTO.setLanes(way.getLanes());
            wayDTO.setName(way.getName());
            wayDTO.setMaxSpeed(way.getMaxSpeed());

            for (WayNode wayNode : wayNodes) {
                if (wayNode.getWayNodeKey().getWay() == way.getId()) {
                    Node node = nodes.stream().filter(n->n.getPoint().getId() == wayNode.getWayNodeKey().getNode()).findFirst().get();
                    int x = (int)((node.getPoint().getLon()-west)/pixelWidth);
                    int y = (int)((node.getPoint().getLat()-south)/pixelHeight);
                    wayDTO.getNodes().add(new NodeDTO(x, y));
                    continue;
                }
            }
            mapDTO.getWays().add(wayDTO);
        }
        return mapDTO;
    }

}
