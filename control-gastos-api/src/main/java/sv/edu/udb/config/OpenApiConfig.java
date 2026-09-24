package sv.edu.udb.config;

package sv.edu.udb.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Control de Gastos Personales",
                version = "2.0.0",
                description = "API REST con arquitectura Event-Driven, Docker y Kubernetes. " +
                        "Permite registrar ingresos y gastos, calcular balance y reaccionar a eventos de negocio.",
                contact = @Contact(name = "Equipo UDB", email = "equipo@udb.edu.sv"),
                license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor local"),
                @Server(url = "http://control-gastos-service:8080", description = "Kubernetes Service")
        }
)
public class OpenApiConfig {
}
