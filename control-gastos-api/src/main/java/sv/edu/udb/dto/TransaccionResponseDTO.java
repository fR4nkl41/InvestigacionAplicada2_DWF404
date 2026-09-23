package sv.edu.udb.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representación de una transacción devuelta por la API")
public class TransaccionResponseDTO {

    private Long id;
    private String descripcion;
    private BigDecimal monto;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;

    private Long categoriaId;
    private String categoriaNombre;
}
