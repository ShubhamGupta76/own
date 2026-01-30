package com.connect.User.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;

/**
 * MongoDB Index Configuration
 * Handles index migrations, particularly for the domain index on organizations collection
 * This runs before Spring Boot's auto-index-creation to fix index conflicts
 */
@Configuration
@Slf4j
@Order(1) // Run early, before index creation
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    public MongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void migrateDomainIndex() {
        try {
            IndexOperations indexOps = mongoTemplate.indexOps("organizations");
            
            // Drop existing domain index if it exists (regardless of sparse setting)
            // We'll recreate it with the correct sparse setting
            boolean indexDropped = false;
            for (org.springframework.data.mongodb.core.index.IndexInfo indexInfo : indexOps.getIndexInfo()) {
                if ("domain".equals(indexInfo.getName())) {
                    log.info("Found existing domain index. Dropping it to recreate with sparse=true");
                    try {
                        indexOps.dropIndex("domain");
                        indexDropped = true;
                        log.info("Successfully dropped domain index");
                        break;
                    } catch (Exception e) {
                        log.warn("Could not drop domain index: {}", e.getMessage());
                    }
                }
            }
            
            // Create the domain index with sparse=true
            if (indexDropped || !indexExists(indexOps, "domain")) {
                log.info("Creating domain index with sparse=true and unique=true");
                org.springframework.data.mongodb.core.index.Index domainIndex = 
                    new org.springframework.data.mongodb.core.index.Index()
                        .on("domain", org.springframework.data.domain.Sort.Direction.ASC)
                        .unique()
                        .sparse()
                        .named("domain");
                indexOps.createIndex(domainIndex);
                log.info("Successfully created sparse domain index");
            }
            
            // Ensure other required indexes exist
            createIndexIfNotExists(indexOps, "name", true, false);
            createIndexIfNotExists(indexOps, "adminId", false, false);
            
        } catch (Exception e) {
            log.error("Error during index migration: {}", e.getMessage(), e);
        }
    }
    
    private boolean indexExists(IndexOperations indexOps, String indexName) {
        return indexOps.getIndexInfo().stream()
            .anyMatch(info -> indexName.equals(info.getName()));
    }
    
    private void createIndexIfNotExists(IndexOperations indexOps, String field, boolean unique, boolean sparse) {
        String indexName = field;
        if (!indexExists(indexOps, indexName)) {
            log.info("Creating index on field: {} (unique: {}, sparse: {})", field, unique, sparse);
            org.springframework.data.mongodb.core.index.Index index = 
                new org.springframework.data.mongodb.core.index.Index()
                    .on(field, org.springframework.data.domain.Sort.Direction.ASC)
                    .named(indexName);
            if (unique) {
                index = index.unique();
            }
            if (sparse) {
                index = index.sparse();
            }
            indexOps.createIndex(index);
        }
    }
}

