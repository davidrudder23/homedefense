package org.noses.homedefense.maps;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class MapsRepository {

    private CassandraOperations template;

    public MapsRepository() {
        init();
    }

    public void init() {
        Cluster cluster = Cluster.builder().addContactPoints("localhost").build();
        Session session = cluster.connect("maps");

        template = new CassandraTemplate(session);
    }

    public void insert(Node node) {
        template.insert(node);
    }

    public void insert(Nest nest) {
        template.insert(nest);
    }

    public void insert(Way way) {
        template.insert(way);
    }

    public void insert(WayNode wayNode) {

        wayNode.getWayNodeKey().setPartitionId(
                Math.round((wayNode.getWayNodeKey().getLon() + 180) * 200) +
                "x" +
                Math.round((wayNode.getWayNodeKey().getLat() + 180) * 200));
        template.insert(wayNode);
    }

    public void insert(Destination destination) {
        destination.getKey().setId(UUID.randomUUID().toString());
        template.insert(destination);
    }

    public void insertNest(Nest nest) {

        nest.getPoint().setPartitionId(
                Math.round((nest.getPoint().getLon() + 180) * 200) +
                        "x" +
                        Math.round((nest.getPoint().getLat() + 180) * 200));
        template.insert(nest);
    }


    public Node getNode(long nodeId) {
        Select select = QueryBuilder.select().from("node")
                .where(QueryBuilder.eq("partition_id", nodeId % 100))
                .and(QueryBuilder.eq("id", nodeId))
                .limit(1);
        List<Node> nodes = template.select(select, Node.class);

        if ((nodes == null) || (nodes.size() == 0)) {
            return null;
        } else {
            return nodes.get(0);
        }
    }

    public List<WayNode> getWayNodes(float north, float west, float south, float east) {
        int northPartitionId = Math.round((north + 180) * 200) + 1;
        int westPartitionId = Math.round((west + 180) * 200) - 1;
        int southPartitionId = Math.round((south + 180) * 200) - 1;
        int eastPartitionId = Math.round((east + 180) * 200) + 1;

        List<String> partitionIds = new ArrayList<>();

        for (int x = westPartitionId; x <= eastPartitionId; x++) {
            for (int y = southPartitionId; y <= northPartitionId; y++) {
                String partitionId = x + "x" + y;
                partitionIds.add(partitionId);
            }
        }

        List<WayNode> wayNodes = partitionIds
                .stream()
                .parallel()
                .map(partitionId -> {
                    Select select = QueryBuilder.select().from("wayNode")
                            .where(QueryBuilder.eq("partition_id", partitionId))
                            .limit(1000000);
                    return template.select(select, WayNode.class);
                }).flatMap(List::stream)
                .collect(Collectors.toList());

        return wayNodes;

    }

    public List<Nest> getNests(float north, float west, float south, float east) {
        int northPartitionId = Math.round((north + 180) * 200);
        int westPartitionId = Math.round((west + 180) * 200);
        int southPartitionId = Math.round((south + 180) * 200);
        int eastPartitionId = Math.round((east + 180) * 200);

        List<String> partitionIds = new ArrayList<>();

        for (int x = westPartitionId; x <= eastPartitionId; x++) {
            for (int y = southPartitionId; y <= northPartitionId; y++) {
                String partitionId = x + "x" + y;
                partitionIds.add(partitionId);
            }
        }

        List<Nest> nests = partitionIds
                .stream()
                .parallel()
                .map(partitionId -> {
                    Select select = QueryBuilder.select().from("nest")
                            .where(QueryBuilder.eq("partition_id", partitionId))
                            .limit(1000000);
                    return template.select(select, Nest.class);
                }).flatMap(List::stream)
                .collect(Collectors.toList());

        return nests;
    }

    public WayNode getPreviousWayNodeForWay(long way, int firstOrder, int partitionId) {
        if (firstOrder < 1) {
            return null;
        }

        Select select = QueryBuilder.select().from("waynode")
                .where(QueryBuilder.eq("partition_id", partitionId))
                .and(QueryBuilder.eq("way", way))
                .and(QueryBuilder.eq("orderNum", firstOrder - 1))
                .limit(1)
                .allowFiltering();
        List<WayNode> wayNodes = template.select(select, WayNode.class);
        if ((wayNodes == null) || (wayNodes.size() == 0)) {
            return null;
        }
        return wayNodes.get(0);
    }

    public WayNode getSubsequentWayNodeForWay(long way, int lastOrder, int partitionId) {
        Select select = QueryBuilder.select().from("waynode")
                .where(QueryBuilder.eq("partition_id", partitionId))
                .and(QueryBuilder.eq("way", way))
                .and(QueryBuilder.eq("orderNum", lastOrder + 1))
                .limit(1);
        List<WayNode> wayNodes = template.select(select, WayNode.class);
        if ((wayNodes == null) || (wayNodes.size() == 0)) {
            return null;
        }
        return wayNodes.get(0);
    }

    public List<WayNode> getWayNodes(List<Long> nodeIds) {
        Select select = QueryBuilder.select().from("waynode")
                .where(QueryBuilder.eq("partition_id", 1))
                .and(QueryBuilder.in("node", nodeIds))
                .limit(nodeIds.size())
                .allowFiltering();
        List<WayNode> wayNodes = template.select(select, WayNode.class);
        return wayNodes;
    }

    public List<Way> getWays(List<Long> wayIds) {
        Select select = QueryBuilder.select().from("way")
                .where(QueryBuilder.in("id", wayIds))
                .limit(100000);
        List<Way> ways = template.select(select, Way.class);
        return ways;
    }

    public List<Destination> getDestinations() {
        Select select = QueryBuilder.select().from("destination")
                .where(QueryBuilder.eq("partition_id", "destination"))
                .limit(100000);
        List<Destination> destinations= template.select(select, Destination.class);
        return destinations;
    }
}
