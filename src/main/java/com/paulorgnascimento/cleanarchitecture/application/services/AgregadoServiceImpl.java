package com.paulorgnascimento.cleanarchitecture.application.services;

import com.paulorgnascimento.cleanarchitecture.application.dto.AgregadoInDto;
import com.paulorgnascimento.cleanarchitecture.application.dto.AgregadoOutDto;
import com.paulorgnascimento.cleanarchitecture.application.mapper.AgregadoMapper;
import com.paulorgnascimento.cleanarchitecture.application.mapper.EntidadeMapper;
import com.paulorgnascimento.cleanarchitecture.domain.aggregateroot.Agregado;
import com.paulorgnascimento.cleanarchitecture.domain.entity.Entidade;
import com.paulorgnascimento.cleanarchitecture.infrastructure.gateway.Integracao;
import com.paulorgnascimento.cleanarchitecture.infrastructure.persistence.entity.AgregadoMapping;
import com.paulorgnascimento.cleanarchitecture.infrastructure.persistence.entity.EntidadeMapping;
import com.paulorgnascimento.cleanarchitecture.infrastructure.persistence.repository.AgregadoRepository;
import com.paulorgnascimento.cleanarchitecture.infrastructure.persistence.repository.EntidadeRepository;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AgregadoServiceImpl implements AgregadoService {

    private final AgregadoMapper agregadoMapper;
    private final EntidadeMapper entidadeMapper;
    private final AgregadoRepository agregadoRepository;
    private final EntidadeRepository entidadeRepository;
    private final IntegracaoService integracaoService;
    @Autowired
    private Tracer tracer;

    public AgregadoServiceImpl(AgregadoMapper agregadoMapper,
                               EntidadeMapper entidadeMapper,
                               AgregadoRepository agregadoRepository,
                               IntegracaoService integracaoService,
                               EntidadeRepository entidadeRepository) {
        this.agregadoMapper = agregadoMapper;
        this.entidadeMapper = entidadeMapper;
        this.agregadoRepository = agregadoRepository;
        this.integracaoService = integracaoService;
        this.entidadeRepository = entidadeRepository;
    }

    @Override
    @Transactional
    public void criarAgregado(AgregadoInDto agregadoInDto) {

        Agregado agregado;
        AgregadoMapping agregadoMapping;

        Span span_integration = tracer.buildSpan("service-integration").start();
        
        span_integration.setTag("tag_personalizada", "algum valor");
        try {
            Integracao integracao = integracaoService.execute(1);
        } finally {
            span_integration.finish();
        }

        Span span_mappings = tracer.buildSpan("service-mappings").start();
        try {
            agregado = agregadoMapper.fromDto(agregadoInDto);
            agregadoMapping = agregadoMapper.toEntity(agregado);
        }
        finally {
            span_mappings.finish();
        }

        Span span_persistence = tracer.buildSpan("service-persistence").start();
        try {
            agregadoRepository.save(agregadoMapping);
            salvarEntidades(agregadoInDto, agregadoMapping);
        } finally {
            span_persistence.finish();
        }

    }

    private void salvarEntidades(AgregadoInDto agregadoInDto, AgregadoMapping agregadoMapping) {
        for (Entidade entidade : agregadoInDto.getCampo1()) {
            EntidadeMapping entidadeMapping = entidadeMapper.toEntity(entidade, agregadoMapping);
            entidadeRepository.save(entidadeMapping);
        }
    }

    @Override
    public AgregadoOutDto consultarAgregado(Long id) {
        Optional<AgregadoMapping> agregadoMappingOptional = agregadoRepository.findById(id);
        return agregadoMappingOptional.map(agregadoMapper::dataMappingToDto).orElse(null);
    }
}
