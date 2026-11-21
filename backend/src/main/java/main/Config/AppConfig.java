package main.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion de Spring. Los repositorios (@Repository) y el Gestor (@Service) los crea Spring:
 * los repositorios reciben su EntityManager y el Gestor los repositorios por constructor.
 */
@Configuration
public class AppConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${bolsines.cors.origen}") String origen) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins(origen).allowedMethods("POST");
            }
        };
    }
}
