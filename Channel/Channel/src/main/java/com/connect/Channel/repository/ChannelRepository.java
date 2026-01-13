package com.connect.Channel.repository;

import com.connect.Channel.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByOrganizationId(Long organizationId);
    List<Channel> findByTeamId(Long teamId);
    List<Channel> findByTeamIdAndActiveTrue(Long teamId);
    java.util.Optional<Channel> findByNameAndTeamId(String name, Long teamId);
}

