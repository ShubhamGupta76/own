package com.connect.Channel.repository;

import com.connect.Channel.entity.ChannelPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelPermissionRepository extends JpaRepository<ChannelPermission, Long> {
    List<ChannelPermission> findByChannelId(Long channelId);
}

