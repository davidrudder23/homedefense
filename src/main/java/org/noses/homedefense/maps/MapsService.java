package org.noses.homedefense.maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MapsService {

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
        for (WayNode wayNode : wayNodes) {
            Long wayId = wayNode.getWay();
            if ((wayNode.getWayNodeKey().getLon() >= west) &&
                    (wayNode.getWayNodeKey().getLon() <= east) &&
                    (wayNode.getWayNodeKey().getLat() >= south) &&
                    (wayNode.getWayNodeKey().getLat() <= north)) {
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

            List<WayNode> wayNodesForWay = wayNodes.stream().filter(wn -> wn.getWay() == way.getId()).collect(Collectors.toList());
            if (wayNodesForWay.size() > 0) {
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

        List<NestDTO> nests = setupNests(mapDTO, height, pixelWidth, pixelHeight, north, west, south, east);
        mapDTO.setNests(nests);
        return mapDTO;
    }

    private List<NestDTO> setupNests(MapDTO mapDTO, int height,
                                     float pixelWidth, float pixelHeight,
                                     float north, float west, float south, float east) {
        List<Nest> nests = mapsRepository.getNests(north, west, south, east);

        if ((nests == null) || (nests.size() == 0)) {
            nests = new ArrayList<>();
            Nest nest = new Nest();

            List<NodeDTO> availableNodes = mapDTO.getWays().stream()
                    .map(w -> w.getNodes())
                    .flatMap(List::stream)
                    .filter(n -> (n.getLat() > south) && (n.getLat() < north) && (n.getLon() > west) && (n.getLon() < east))
                    .collect(Collectors.toList());

            NodeDTO node = availableNodes.get((int) (Math.random() * availableNodes.size()));

            nest.setPoint(new NestPoint(Nest.getPartitionId(node.getLat(), node.getLon()), node.getId(), node.getLat(), node.getLon()));

            nests.add(nest);
            mapsRepository.insert(nest);
        }

        List<NestDTO> nestDTOs = nests.stream()
                .map(nest -> {
                    int x = (int) ((nest.getPoint().getLon() - west) / pixelWidth);
                    int y = height - (int) ((nest.getPoint().getLat() - south) / pixelHeight);
                    return new NestDTO(x, y, nest.getPoint().getLat(), nest.getPoint().getLon(), nest.getPoint().getId(), "standard");
                })
                .collect(Collectors.toList());

        return nestDTOs;
    }

}
