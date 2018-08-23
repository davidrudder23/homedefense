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
import java.util.stream.Collectors;

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

    public void insertNode(Node node) {
        template.insert(node);
    }

    public void insertWay(Way way) {
        template.insert(way);
    }

    public void insertWayNode(WayNode wayNode) {
        template.insert(wayNode);
    }

    public Node getNode(long nodeId) {
        Select select = QueryBuilder.select().from("node")
                .where(QueryBuilder.eq("partition_id", nodeId % 100))
                .and(QueryBuilder.eq("id", nodeId))
                .limit(1);
        List<Node> nodes = template.select(select, Node.class);

        if ((nodes == null) || (nodes.size()==0)) {
            return null;
        } else {
            return nodes.get(0);
        }
    }

    public List<Node> getNodes(float north, float west, float south, float east) {
        int westPartitionId = Math.round((west + 180) / 10);
        int eastPartitionId = Math.round((east + 180) / 10);

        List<Node> nodes = new ArrayList<>();
        for (int partitionId = westPartitionId; partitionId <= eastPartitionId; partitionId++) {
            Select select = QueryBuilder.select().from("node")
                    .where(QueryBuilder.eq("partition_id", westPartitionId))
                    .and(QueryBuilder.lte("lat", north))
                    .and(QueryBuilder.gte("lat", south))
                    .and(QueryBuilder.lte("lon", east))
                    .and(QueryBuilder.gte("lon", west))
                    .limit(100000)
                    .allowFiltering();
            nodes.addAll(template.select(select, Node.class));
        }
        return nodes;
    }

    public List<WayNode> getWayNodes(float north, float west, float south, float east) {
        int westPartitionId = Math.round((west + 180) / 10);
        int eastPartitionId = Math.round((east + 180) / 10);

        List<WayNode> wayNodes = new ArrayList<>();
        for (int partitionId = westPartitionId; partitionId <= eastPartitionId; partitionId++) {
            Select select = QueryBuilder.select().from("wayNode")
                    .where(QueryBuilder.eq("partition_id", partitionId))
                    .and(QueryBuilder.lte("lat", north))
                    .and(QueryBuilder.gte("lat", south))
                    .and(QueryBuilder.lte("lon", east))
                    .and(QueryBuilder.gte("lon", west))
                    .limit(100000)
                    .allowFiltering();
            wayNodes.addAll(template.select(select, WayNode.class));
        }
        return wayNodes;
    }

    public List<Node> getNodesForWays(List<Long> nodeIds) {
        Select select = QueryBuilder.select().from("node")
                .where(QueryBuilder.eq("partition_id", 8))
                .and(QueryBuilder.in("id", nodeIds))
                .limit(100000);
        List<Node> nodes = template.select(select, Node.class);
        return nodes;
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
}
