package com.baseplus.modules.audit.service;

import java.util.List;
import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.modules.audit.domain.AuditLog;
import com.baseplus.modules.audit.dto.AuditLogResponse;
import com.baseplus.modules.audit.repository.AuditLogRepository;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.service.UsuarioService;
import com.baseplus.shared.dto.PageResponse;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UsuarioService usuarioService;

    public AuditLogService(AuditLogRepository auditLogRepository, UsuarioService usuarioService) {
        this.auditLogRepository = auditLogRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public AuditLogResponse registrar(String usuario, String acao, String entidade, Long entidadeId) {
        AuditLog auditLog = auditLogRepository.save(new AuditLog(
                normalizarUsuario(usuario),
                normalizar(acao),
                normalizar(entidade),
                entidadeId
        ));

        return toResponse(auditLog);
    }

    @Transactional
    public AuditLogResponse registrarAutenticado(String acao, String entidade, Long entidadeId) {
        return registrar(getUsuarioAutenticado(), acao, entidade, entidadeId);
    }

    public PageResponse<AuditLogResponse> listar(
            String search,
            String acao,
            String entidade,
            String usuario,
            OffsetDateTime dataInicial,
            OffsetDateTime dataFinal,
            Pageable pageable
    ) {
        Pageable resolvedPageable = resolvePageable(pageable);
        Specification<AuditLog> specification = matchesSearch(search);
        specification = and(specification, matchesUsuario(usuario));
        specification = and(specification, matchesAcao(acao));
        specification = and(specification, matchesEntidade(entidade));
        specification = and(specification, matchesDataInicial(dataInicial));
        specification = and(specification, matchesDataFinal(dataFinal));

        Page<AuditLog> page = auditLogRepository.findAll(specification, resolvedPageable);
        List<AuditLogResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.from(page, content);
    }

    private String getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            return "SYSTEM";
        }

        try {
            Long usuarioId = Long.valueOf(authentication.getPrincipal().toString());
            return usuarioService.buscarPorId(usuarioId)
                    .map(this::formatarUsuario)
                    .orElse("SYSTEM");
        } catch (NumberFormatException exception) {
            return "SYSTEM";
        }
    }

    private String formatarUsuario(Usuario usuario) {
        if (usuario == null) {
            return "SYSTEM";
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return normalizarUsuario(usuario.getNome());
        }

        return usuario.getEmail().trim().toLowerCase();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getUsuario(),
                auditLog.getAcao(),
                auditLog.getEntidade(),
                auditLog.getEntidadeId(),
                auditLog.getTimestamp()
        );
    }

    private String normalizarUsuario(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            return "SYSTEM";
        }

        return usuario.trim();
    }

    private String normalizarUsuarioFiltro(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            return null;
        }

        return usuario.trim();
    }

    private String normalizar(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase();
    }

    private Pageable resolvePageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 10, Sort.by(Sort.Order.desc("timestamp"), Sort.Order.desc("id")));
        }

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Order.desc("timestamp"), Sort.Order.desc("id"))
            );
        }

        return pageable;
    }

    private Specification<AuditLog> matchesUsuario(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            return null;
        }

        String term = "%" + usuario.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("usuario")), term);
    }

    private Specification<AuditLog> matchesSearch(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        String term = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("usuario")), term),
                cb.like(cb.lower(root.get("acao")), term),
                cb.like(cb.lower(root.get("entidade")), term)
        );
    }

    private Specification<AuditLog> matchesAcao(String acao) {
        if (acao == null || acao.trim().isEmpty()) {
            return null;
        }

        String term = "%" + acao.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("acao")), term);
    }

    private Specification<AuditLog> matchesEntidade(String entidade) {
        if (entidade == null || entidade.trim().isEmpty()) {
            return null;
        }

        String term = "%" + entidade.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("entidade")), term);
    }

    private Specification<AuditLog> matchesDataInicial(OffsetDateTime dataInicial) {
        if (dataInicial == null) {
            return null;
        }

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), dataInicial);
    }

    private Specification<AuditLog> matchesDataFinal(OffsetDateTime dataFinal) {
        if (dataFinal == null) {
            return null;
        }

        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), dataFinal);
    }

    private Specification<AuditLog> and(Specification<AuditLog> current, Specification<AuditLog> next) {
        if (current == null) {
            return next;
        }

        if (next == null) {
            return current;
        }

        return current.and(next);
    }
}
