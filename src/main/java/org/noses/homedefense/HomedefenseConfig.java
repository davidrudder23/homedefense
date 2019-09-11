package org.noses.homedefense;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DropKeyspaceSpecification;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableCassandraRepositories(basePackages = { "org.noses.homedefense" })
public class HomedefenseConfig extends AbstractCassandraConfiguration {
    public static final String KEYSPACE = "maps";

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(KEYSPACE);
        specification = specification.ifNotExists(true);

        return Arrays.asList(specification);
        //return new ArrayList<>();
    }

    @Override
    protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
        return new ArrayList<>();
        //Arrays.asList(DropKeyspaceSpecification.dropKeyspace(KEYSPACE));
    }

    @Override
    protected String getKeyspaceName() {
        return KEYSPACE;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"org.noses.homedefense"};
    }

}
