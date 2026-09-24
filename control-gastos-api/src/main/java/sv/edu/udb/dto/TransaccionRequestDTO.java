TransaccionRequestDTO.java:

package sv.edu.udb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos necesarios para registrar una transacción")
public class TransaccionRequestDTO {

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 100, message = "La descripción no puede exceder 100 caracteres")
    @Schema(description = "Descripción de la transacción", example = "Cena en restaurante")
    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    @Schema(description = "Monto de la transacción en USD", example = "25.50")
    private BigDecimal monto;

    @NotNull(message = "La categoría es obligatoria")
    @Schema(description = "ID de la categoría asociada", example = "2")
    private Long categoriaId;
}
