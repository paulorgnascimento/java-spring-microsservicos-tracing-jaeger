package com.paulorgnascimento.cleanarchitecture.infrastructure.config;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.samplers.ConstSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JaegerConfig {

    @Bean
    public JaegerTracer jaegerTracer() {
        io.jaegertracing.Configuration.SamplerConfiguration samplerConfig = io.jaegertracing.Configuration.SamplerConfiguration.fromEnv().withType(ConstSampler.TYPE).withParam(1);
        io.jaegertracing.Configuration.ReporterConfiguration reporterConfig = io.jaegertracing.Configuration.ReporterConfiguration.fromEnv().withLogSpans(true).withSender(
                io.jaegertracing.Configuration.SenderConfiguration.fromEnv()
                        .withAgentHost("localhost")
                        .withAgentPort(6831)
        );
        io.jaegertracing.Configuration config = new io.jaegertracing.Configuration("paulo-microservice").withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }
}
