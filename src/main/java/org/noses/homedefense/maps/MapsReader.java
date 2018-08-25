package org.noses.homedefense.maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
                .sorted(Comparator.comparingInt(WayNode::getOrderNum))
                .collect(Collectors.toList());
        HashMap<Long, Way> ways = new HashMap<>();
        for (WayNode wayNode: wayNodes) {
            Long wayId = wayNode.getWay();
            if ((wayNode.getWayNodeKey().getLon()>=west) &&
                    (wayNode.getWayNodeKey().getLon()<=east) &&
                    (wayNode.getWayNodeKey().getLat()>=south) &&
                    (wayNode.getWayNodeKey().getLat()<=north)) {
                Way way = ways.get(wayId);
                if (way == null) {
                    way = new Way();
                    way.setHighway(wayNode.getHighway());
                    way.setId(wayNode.getWay());
                    way.setName(wayNode.getName());
                    way.setLanes(wayNode.getLanes());
                    way.setMaxSpeed(wayNode.getMaxSpeed());
                    way.setOneWay(wayNode.isOneWay());
                    ways.put(new Long(wayNode.getWay()), way);
                }
            }
        }

        MapDTO mapDTO = new MapDTO();
        mapDTO.setWidth(width);
        mapDTO.setHeight(height);

        float pixelHeight = Math.abs(north - south) / height;
        float pixelWidth = Math.abs(east - west) / width;

        for (Way way : ways.values()) {
            WayDTO wayDTO = new WayDTO();
            wayDTO.setLanes(way.getLanes());
            wayDTO.setName(way.getName());
            wayDTO.setMaxSpeed(way.getMaxSpeed());
            wayDTO.setHighway(way.getHighway());

            List<WayNode> wayNodesForWay = wayNodes.stream().filter(wn->wn.getWay() == way.getId()).collect(Collectors.toList());
            if (wayNodesForWay.size()>0) {
                for (WayNode wayNode : wayNodesForWay) {
                    int x = (int) ((wayNode.getWayNodeKey().getLon() - west) / pixelWidth);
                    int y = height - (int) ((wayNode.getWayNodeKey().getLat() - south) / pixelHeight);
                    wayDTO.getNodes().add(new NodeDTO(x, y,
                            wayNode.getWayNodeKey().getLat(), wayNode.getWayNodeKey().getLon(),
                            wayNode.getWayNodeKey().getId(), wayNode.getOrderNum()));
                }
                mapDTO.getWays().add(wayDTO);
            }
        }
        return mapDTO;
    }

}
