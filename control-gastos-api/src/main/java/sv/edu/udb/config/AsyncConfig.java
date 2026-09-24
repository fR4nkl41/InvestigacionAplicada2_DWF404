AsyncConfig.java:

package sv.edu.udb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita la ejecución asíncrona con @Async en toda la aplicación.
 * Los listeners de eventos se ejecutarán en hilos del pool configurado
 * en application.properties (spring.task.execution.*).
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // La configuración del pool se toma de application.properties
}
