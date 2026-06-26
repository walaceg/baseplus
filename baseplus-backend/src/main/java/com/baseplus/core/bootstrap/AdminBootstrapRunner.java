package com.baseplus.core.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.usuario.dto.UsuarioResponse;

@Component
@Order
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final AdminBootstrapProperties properties;
    private final AdminBootstrapService adminBootstrapService;
    private final ApplicationContext applicationContext;

    public AdminBootstrapRunner(
            AdminBootstrapProperties properties,
            AdminBootstrapService adminBootstrapService,
            ApplicationContext applicationContext
    ) {
        this.properties = properties;
        this.adminBootstrapService = adminBootstrapService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }

        int exitCode = 0;
        try {
            UsuarioResponse response = adminBootstrapService.bootstrap(
                    properties.getName(),
                    properties.getEmail(),
                    properties.getPassword()
            );
            logger.info("Bootstrap administrativo concluido para o usuario {}.", response.email());
        } catch (BusinessException exception) {
            exitCode = 1;
            logger.error("Bootstrap administrativo recusado: {}", exception.getMessage());
        } catch (RuntimeException exception) {
            exitCode = 1;
            logger.error("Bootstrap administrativo falhou.", exception);
        }

        int resolvedExitCode = exitCode;
        int springExitCode = SpringApplication.exit(applicationContext, () -> resolvedExitCode);
        System.exit(springExitCode);
    }
}
