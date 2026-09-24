TransaccionController.java:

package sv.edu.udb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.dto.TransaccionRequestDTO;
import sv.edu.udb.dto.TransaccionResponseDTO;
import sv.edu.udb.service.TransaccionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Operaciones CRUD sobre transacciones y balance")
public class TransaccionController {

    private final TransaccionService transaccionService;

    @Operation(summary = "Registrar una transacción",
            description = "Publica un evento Event-Driven al guardar (auditoría + notificación asíncrona)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacción registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @PostMapping
    public ResponseEntity<TransaccionResponseDTO> registrar(
            @Valid @RequestBody TransaccionRequestDTO dto) {
        return new ResponseEntity<>(transaccionService.registrarTransaccion(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Listar todas las transacciones")
    @GetMapping
    public ResponseEntity<List<TransaccionResponseDTO>> listar() {
        return ResponseEntity.ok(transaccionService.listarTransacciones());
    }

    @Operation(summary = "Obtener transacción por ID")
    @GetMapping("/{id}")
    public ResponseEntity<TransaccionResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerPorId(id));
    }

    @Operation(summary = "Calcular balance general (ingresos - gastos)")
    @GetMapping("/balance")
    public ResponseEntity<Map<String, BigDecimal>> balance() {
        return ResponseEntity.ok(Map.of("balance", transaccionService.calcularBalance()));
    }

    @Operation(summary = "Listar transacciones por categoría")
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<TransaccionResponseDTO>> porCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(transaccionService.listarPorCategoria(categoriaId));
    }

    @Operation(summary = "Eliminar una transacción",
            description = "Publica evento de eliminación para recalcular balance")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transacción eliminada"),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        transaccionService.eliminarTransaccion(id);
        return ResponseEntity.noContent().build();
    }
}
