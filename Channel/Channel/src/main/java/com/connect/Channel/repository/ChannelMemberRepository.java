package com.connect.Channel.repository;

import com.connect.Channel.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {
    List<ChannelMember> findByChannelId(Long channelId);
    java.util.Optional<ChannelMember> findByChannelIdAndUserId(Long channelId, Long userId);
    boolean existsByChannelIdAndUserId(Long channelId, Long userId);
}

