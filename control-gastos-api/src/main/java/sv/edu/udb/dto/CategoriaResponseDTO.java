package sv.edu.udb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representación de una categoría devuelta por la API")
public class CategoriaResponseDTO {
    @Schema(description = "ID de la categoría", example = "1")
    private Long id;
    @Schema(description = "Nombre de la categoría", example = "Comida")
    private String nombre;
    @Schema(description = "Tipo de categoría", example = "GASTO")
    private String tipo;
}
