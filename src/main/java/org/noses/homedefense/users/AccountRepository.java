package org.noses.homedefense.users;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
public class AccountRepository {
    private CassandraOperations template;

    InsertOptions insertOptions;

    public AccountRepository() {
        Cluster cluster = Cluster.builder().addContactPoints("localhost").build();
        Session session = cluster.connect("session");

        template = new CassandraTemplate(session);

        insertOptions = InsertOptions.builder()
                .ttl(Duration.ofHours(24))
                .build();
    }

    public void save(AccountSession accountSession) {
        template.insert(accountSession, insertOptions);

    }

    public void save(Account account) {
        template.insert(account, insertOptions);

    }

    public Account getAccountBySessionToken(String sessionToken) {
        Select select = QueryBuilder.select().from("account_session")
                .where(QueryBuilder.eq("partition_id", AccountSession.getPartitionIdFromToken(sessionToken)))
                .and(QueryBuilder.eq("token", sessionToken))
                .limit(1);
        List<AccountSession> sessions = template.select(select, AccountSession.class);
        if ((sessions == null) || (sessions.size()==0)) {
            return null;
        }
        AccountSession session = sessions.get(0);

        select = QueryBuilder.select().from("account_session")
                .where(QueryBuilder.eq("partition_id", session.getAccountPartitionId()))
                .and(QueryBuilder.eq("username", session.getUsername()))
                .limit(1);
        List<Account> accounts = template.select(select, Account.class);

        if ((accounts == null) || (accounts.size()==0)) {
            return null;
        }
        return  accounts.get(0);
    }

    public Account getAccountByUsername(String username) {
        Select select = QueryBuilder.select().from("account")
                .where(QueryBuilder.eq("partition_id", Account.getPartitionIdByUsername(username)))
                .and(QueryBuilder.eq("username", username))
                .limit(1);
        List<Account> accounts = template.select(select, Account.class);

        if ((accounts == null) || (accounts.size()==0)) {
            return null;
        }

        return accounts.get(0);
    }

}
