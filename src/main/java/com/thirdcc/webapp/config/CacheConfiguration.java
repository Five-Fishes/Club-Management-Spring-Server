package com.thirdcc.webapp.config;

import java.time.Duration;

import org.ehcache.config.builders.*;
import org.ehcache.jsr107.Eh107Configuration;

import org.hibernate.cache.jcache.ConfigSettings;
import io.github.jhipster.config.JHipsterProperties;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Ehcache ehcache =
            jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
                ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                .build());
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, com.thirdcc.webapp.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, com.thirdcc.webapp.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, com.thirdcc.webapp.domain.User.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.Authority.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.User.class.getName() + ".authorities");
            createCache(cm, com.thirdcc.webapp.domain.Event.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.EventCrew.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.EventAttendee.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.ImageStorage.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.EventImage.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.Budget.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.EventActivity.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.EventChecklist.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.Transaction.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.Receipt.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.Administrator.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.UserCCInfo.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.UserUniInfo.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.YearSession.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.Faculty.class.getName());
            createCache(cm, com.thirdcc.webapp.domain.EventRegistrationClosingCriteria.class.getName());
            // jhipster-needle-ehcache-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cm.destroyCache(cacheName);
        }
        cm.createCache(cacheName, jcacheConfiguration);
    }
}
