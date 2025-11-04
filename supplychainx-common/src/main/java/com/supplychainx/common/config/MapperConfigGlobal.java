package com.supplychainx.common.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.context.annotation.Configuration;


@Configuration
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public class MapperConfigGlobal {
}
