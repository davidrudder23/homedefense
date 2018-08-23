package org.noses.homedefense.maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
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
        List<WayNode> wayNodes = mapsRepository.getWayNodes(north, west, south, east)
                .stream()
                .sorted(Comparator.comparingInt(WayNode::getOrder))
                .collect(Collectors.toList());
        List<Way> ways = mapsRepository.getWays(wayNodes.stream()
                .map(wn -> wn.getWayNodeKey().getWay())
                .collect(Collectors.toList()));
        // Now, get all the nodes for the ways
        List<Long> nodeIds = new ArrayList<>();
        for (Way way: ways) {
            for (WayNode wayNode: wayNodes) {
                if (wayNode.getWayNodeKey().getWay() == way.getId()) {
                    continue;
                }
            }
        }

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
            wayDTO.setHighway(way.getHighway());

            for (WayNode wayNode : wayNodes) {
                if (wayNode.getWayNodeKey().getWay() == way.getId()) {
                    int x = (int)((wayNode.getWayNodeKey().getLon()-west)/pixelWidth);
                    int y = height-(int)((wayNode.getWayNodeKey().getLat()-south)/pixelHeight);
                    wayDTO.getNodes().add(new NodeDTO(x, y,
                            wayNode.getWayNodeKey().getLat(), wayNode.getWayNodeKey().getLon(),
                            wayNode.getWayNodeKey().getId(), wayNode.getOrder()));
                    continue;
                }
            }
            mapDTO.getWays().add(wayDTO);
        }
        return mapDTO;
    }

}
