package com.connect.Channel.repository;

import com.connect.Channel.entity.ChannelPermission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelPermissionRepository extends MongoRepository<ChannelPermission, String> {
    List<ChannelPermission> findByChannelId(String channelId);
}

