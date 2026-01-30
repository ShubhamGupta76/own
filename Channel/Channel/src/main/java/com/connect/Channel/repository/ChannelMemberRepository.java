package com.connect.Channel.repository;

import com.connect.Channel.entity.ChannelMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelMemberRepository extends MongoRepository<ChannelMember, String> {
    List<ChannelMember> findByChannelId(String channelId);
    Optional<ChannelMember> findByChannelIdAndUserId(String channelId, String userId);
    boolean existsByChannelIdAndUserId(String channelId, String userId);
}

