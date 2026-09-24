package sv.edu.udb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos necesarios para crear una categoría")
public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Schema(description = "Nombre de la categoría", example = "Comida")
    private String nombre;

    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "INGRESO|GASTO", message = "El tipo debe ser INGRESO o GASTO")
    @Schema(description = "Tipo de categoría", example = "GASTO",
            allowableValues = {"INGRESO", "GASTO"})
    private String tipo;
}
