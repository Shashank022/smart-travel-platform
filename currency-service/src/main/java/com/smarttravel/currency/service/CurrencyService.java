package com.smarttravel.currency.service;

import com.smarttravel.currency.dto.CurrencyConversionResponse;
import com.smarttravel.currency.dto.FrankfurterRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Service
public class CurrencyService {

    private static final Logger log =
            LoggerFactory.getLogger(CurrencyService.class);

    private static final Duration CURRENCY_CACHE_TTL =
            Duration.ofHours(1);

    private final WebClient webClient;
    private final ReactiveStringRedisTemplate redisTemplate;

    public CurrencyService(
            WebClient.Builder builder,
            ReactiveStringRedisTemplate redisTemplate,
            @Value("${clients.frankfurter.base-url}") String baseUrl) {

        this.webClient = builder
                .baseUrl(baseUrl)
                .build();

        this.redisTemplate = redisTemplate;
    }

    public Mono<CurrencyConversionResponse> convert(
            String from,
            String to,
            BigDecimal amount) {

        String normalizedFrom =
                from.trim().toUpperCase(Locale.ROOT);

        String normalizedTo =
                to.trim().toUpperCase(Locale.ROOT);

        if (normalizedFrom.equals(normalizedTo)) {

            return Mono.just(
                    buildConversionResponse(
                            normalizedFrom,
                            normalizedTo,
                            BigDecimal.ONE,
                            amount
                    )
            );
        }

        String cacheKey =
                "currency:"
                        + normalizedFrom
                        + ":"
                        + normalizedTo;

        return getRate(
                cacheKey,
                normalizedFrom,
                normalizedTo
        ).map(rate ->
                buildConversionResponse(
                        normalizedFrom,
                        normalizedTo,
                        rate,
                        amount
                )
        );
    }

    private Mono<BigDecimal> getRate(
            String cacheKey,
            String from,
            String to) {

        return redisTemplate
                .opsForValue()
                .get(cacheKey)

                .map(BigDecimal::new)

                .doOnNext(rate ->
                        log.info(
                                "Redis HIT: {} rate={}",
                                cacheKey,
                                rate
                        )
                )

                .onErrorResume(exception -> {

                    log.warn(
                            "Redis read failed for {}. Calling Frankfurter.",
                            cacheKey
                    );

                    return Mono.empty();
                })

                .switchIfEmpty(
                        Mono.defer(() -> {

                            log.info(
                                    "Redis MISS: {}",
                                    cacheKey
                            );

                            return fetchRateFromFrankfurter(
                                    from,
                                    to
                            ).flatMap(rate ->

                                    redisTemplate
                                            .opsForValue()
                                            .set(
                                                    cacheKey,
                                                    rate.toPlainString(),
                                                    CURRENCY_CACHE_TTL
                                            )

                                            .doOnNext(saved ->
                                                    log.info(
                                                            "Stored {} in Redis with TTL {}",
                                                            cacheKey,
                                                            CURRENCY_CACHE_TTL
                                                    )
                                            )

                                            .thenReturn(rate)

                                            .onErrorResume(exception -> {

                                                log.warn(
                                                        "Unable to cache {}",
                                                        cacheKey
                                                );

                                                return Mono.just(rate);
                                            })
                            );
                        })
                );
    }

    private Mono<BigDecimal> fetchRateFromFrankfurter(
            String from,
            String to) {

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/rates")
                        .queryParam("base", from)
                        .queryParam("quotes", to)
                        .build())
                .retrieve()
                .bodyToFlux(FrankfurterRate.class)
                .collectList()
                .flatMap(this::extractRate);
    }

    private Mono<BigDecimal> extractRate(
            List<FrankfurterRate> rates) {

        if (rates.isEmpty()) {

            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.BAD_GATEWAY,
                            "Exchange rate unavailable"
                    )
            );
        }

        return Mono.just(
                rates.getFirst().rate()
        );
    }

    private CurrencyConversionResponse buildConversionResponse(
            String from,
            String to,
            BigDecimal rate,
            BigDecimal amount) {

        BigDecimal converted =
                amount
                        .multiply(rate)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new CurrencyConversionResponse(
                from,
                to,
                rate,
                amount,
                converted
        );
    }
}