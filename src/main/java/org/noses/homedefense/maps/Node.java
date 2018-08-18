package org.noses.homedefense.maps;

import com.datastax.driver.core.DataType;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("node")
@Data
public class Node {

    @PrimaryKey
    @CassandraType(type = DataType.Name.BIGINT)
    private long id;

    private float lat;
    private float lon;

}
