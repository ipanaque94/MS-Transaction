package com.enoc.transaction.application.service.cache;

import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.repository.TransactionRepository;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import com.enoc.transaction.infrastructure.mapper.TransactionMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveCachedTransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final ReactiveRedisTemplate<String, TransactionResponseDto> redisTemplate;

    // TTL por tipo de dato
    private static final Duration TTL_TRANSACTIONS = Duration.ofMinutes(15);
    private static final Duration TTL_ACTIVE = Duration.ofMinutes(5);
    private static final Duration TTL_LAST = Duration.ofMinutes(10);

    /*
      Method to get a transaction by its ID using Redis cache.
      Método para obtener una transacción por su ID usando caché en Redis.
 */

    public Mono<TransactionResponseDto> getByIdCached(String id) {
        String key = "transactions::" + id;
        return redisTemplate.opsForValue().get(key)
                .switchIfEmpty(repository.findById(id)
                        .map(mapper::toDto)
                        .flatMap(dto -> redisTemplate.opsForValue()
                                .set(key, dto, TTL_TRANSACTIONS)
                                .thenReturn(dto)));
    }
    /*
      Method to get an active transaction by its ID using Redis cache.
      Método para obtener una transacción activa por su ID usando caché en Redis.
 */

    public Mono<TransactionResponseDto> getActiveByIdCached(String id) {
        String key = "activeTransactions::" + id;
        return redisTemplate.opsForValue().get(key)
                .switchIfEmpty(repository.findByIdAndState(id, TransactionState.ACTIVE)
                        .map(mapper::toDto)
                        .flatMap(dto -> redisTemplate.opsForValue()
                                .set(key, dto, TTL_ACTIVE)
                                .thenReturn(dto)));
    }

    /*
      Method to get the last active transaction by customer ID using Redis cache.
      Método para obtener la última transacción activa por ID de cliente usando caché en Redis.
 */


    public Mono<TransactionResponseDto> getLastByCustomerIdCached(String customerId) {
        String key = "lastTransactions::" + customerId;
        return redisTemplate.opsForValue().get(key)
                .switchIfEmpty(repository.findTopByCustomerIdAndStateOrderByCreatedAtDesc(customerId, TransactionState.ACTIVE)
                        .map(mapper::toDto)
                        .flatMap(dto -> redisTemplate.opsForValue()
                                .set(key, dto, TTL_LAST)
                                .thenReturn(dto)));
    }
}