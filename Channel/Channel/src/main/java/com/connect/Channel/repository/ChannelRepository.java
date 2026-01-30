package com.connect.Channel.repository;

import com.connect.Channel.entity.Channel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends MongoRepository<Channel, String> {
    List<Channel> findByOrganizationId(String organizationId);
    List<Channel> findByTeamId(String teamId);
    List<Channel> findByTeamIdAndActiveTrue(String teamId);
    Optional<Channel> findByNameAndTeamId(String name, String teamId);
}

