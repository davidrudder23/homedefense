package org.noses.homedefense.drops;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DropRepository {
    private CassandraOperations template;

    InsertOptions insertOptions;

    public DropRepository() {
        Cluster cluster = Cluster.builder().addContactPoints("localhost").build();
        Session session = cluster.connect("maps");

        template = new CassandraTemplate(session);

        insertOptions = InsertOptions.builder()
                .ttl(Duration.ofHours(24))
                .build();
    }

    public void save(Drop drop) {
        drop.setPartitionId();
        template.insert(drop, insertOptions);
    }

    public List<Drop> getDropsByGeo(double north, double west, double south, double east) {
        long northPartitionId = Math.round((north + 180) * 200) + 1;
        long westPartitionId = Math.round((west + 180) * 200) - 1;
        long southPartitionId = Math.round((south + 180) * 200) - 1;
        long eastPartitionId = Math.round((east + 180) * 200) + 1;

        List<String> partitionIds = new ArrayList<>();

        for (long x = westPartitionId; x <= eastPartitionId; x++) {
            for (long y = southPartitionId; y <= northPartitionId; y++) {
                String partitionId = x + "x" + y;
                partitionIds.add(partitionId);
            }
        }

        List<Drop> drops = partitionIds
                .stream()
                .parallel()
                .map(partitionId -> {
                    Select select = QueryBuilder.select().from("drops")
                            .where(QueryBuilder.eq("partition_id", partitionId))
                            .limit(1000000);
                    return template.select(select, Drop.class);
                }).flatMap(List::stream)
                .collect(Collectors.toList());

        return drops;

    }


}
