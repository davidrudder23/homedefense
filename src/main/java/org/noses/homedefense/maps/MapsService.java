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

    public MapDTO readMap(double north,
                          double south,
                          double east,
                          double west) {
        List<WayNode> wayNodes = mapsRepository.getWayNodes(north, west, south, east)
                .stream()
                .sorted(Comparator.comparingInt(WayNode::getOrderNum))
                .collect(Collectors.toList());
        HashMap<Long, Way> ways = new HashMap<>();
        for (WayNode wayNode : wayNodes) {
            Long wayId = wayNode.getWay();
            System.out.println(wayNode.getWayNodeKey().getLon() + " vs " + south);
            System.out.println(wayNode.getWayNodeKey().getLat() + " vs " + east);
            if ((wayNode.getWayNodeKey().getLat() >= south) &&
                    (wayNode.getWayNodeKey().getLat() <= north) &&
                    (wayNode.getWayNodeKey().getLon() >= west) &&
                    (wayNode.getWayNodeKey().getLon() <= east)) {
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

        System.out.println("Ways size=" + ways.size());

        MapDTO mapDTO = new MapDTO();

        mapDTO.setNorth(north);
        mapDTO.setSouth(south);
        mapDTO.setEast(east);
        mapDTO.setWest(west);

        for (Way way : ways.values()) {
            WayDTO wayDTO = new WayDTO();
            wayDTO.setLanes(way.getLanes());
            wayDTO.setName(way.getName());
            wayDTO.setMaxSpeed(way.getMaxSpeed());
            wayDTO.setHighway(way.getHighway());

            List<WayNode> wayNodesForWay = wayNodes.stream().filter(wn -> wn.getWay() == way.getId()).collect(Collectors.toList());
            if (wayNodesForWay.size() > 0) {
                for (WayNode wayNode : wayNodesForWay) {
                    wayDTO.getNodes().add(new NodeDTO(
                            wayNode.getWayNodeKey().getLat(), wayNode.getWayNodeKey().getLon(),
                            wayNode.getWayNodeKey().getId(), wayNode.getOrderNum()));
                }
                System.out.println("Adding way to map " + wayDTO.getName());
                mapDTO.getWays().add(wayDTO);
            }
        }

        //List<NestDTO> nests = setupNests(mapDTO, north, west, south, east);
        //mapDTO.setNests(nests);
        return mapDTO;
    }

    private List<NestDTO> setupNests(MapDTO mapDTO,
                                     double north, double west, double south, double east) {
        List<Nest> nests = mapsRepository.getNests(north, west, south, east);

        while ((nests == null) || (nests.size() < 3)) {
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
            nests = mapsRepository.getNests(north, west, south, east);
        }

        List<NestDTO> nestDTOs = nests.stream()
                .map(nest -> {
                    return new NestDTO(nest.getPoint().getLat(), nest.getPoint().getLon(), nest.getPoint().getId(), "standard");
                })
                .collect(Collectors.toList());

        return nestDTOs;
    }

    public List<DestinationDTO> getDestinations() {
        List<Destination> destinations = mapsRepository.getDestinations();
        return destinations.stream().map(d -> new DestinationDTO(d)).collect(Collectors.toList());
    }

}
