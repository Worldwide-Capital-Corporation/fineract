/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.farmersbank.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisCacheService implements CacheService {

    private JedisPool pool;

    @Value("${fineract.tenant.redis.host}")
    private String redisHost;

    @Value("${fineract.tenant.redis.token}")
    private String redisToken;


    public RedisCacheService() {
        this.pool = new JedisPool(redisHost, 6379, null, redisToken);
    }

    @Override
    public void set(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, value);
        }
    }

    @Override
    public void set(String key, String value, Long expiresIn) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, value);
            jedis.expire(key, expiresIn);
        }
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(key)) {
               return jedis.get(key);
            }
            return null;
        }
    }
}
