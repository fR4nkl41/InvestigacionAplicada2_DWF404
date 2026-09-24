ErrorResponseDTO.java:

package sv.edu.udb.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estructura estándar de errores de la API")
public class ErrorResponseDTO {

    @Schema(description = "Momento en que ocurrió el error", example = "2026-09-21T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Código HTTP", example = "404")
    private int status;

    @Schema(description = "Descripción corta del error", example = "Not Found")
    private String error;

    @Schema(description = "Mensaje detallado del error", example = "Transacción no encontrada con id: 99")
    private String message;

    @Schema(description = "Ruta del endpoint que generó el error", example = "/api/transacciones/99")
    private String path;

    @Schema(description = "Errores de validación por campo (si aplica)")
    private Map<String, String> validationErrors;
}
