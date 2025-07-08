package com.discussion.repository;

import com.discussion.entities.Reaction;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface ReactionRepoCassandra extends CassandraRepository<Reaction, Long> {

}
