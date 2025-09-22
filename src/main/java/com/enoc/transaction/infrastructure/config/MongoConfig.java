package com.enoc.transaction.infrastructure.config;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new Converter<OffsetDateTime, Date>() {
                    @Override
                    public Date convert(OffsetDateTime source) {
                        return Date.from(source.toInstant());
                    }
                },
                new Converter<Date, OffsetDateTime>() {
                    @Override
                    public OffsetDateTime convert(Date source) {
                        return source.toInstant().atOffset(ZoneOffset.UTC);
                    }
                }
        ));
    }
}