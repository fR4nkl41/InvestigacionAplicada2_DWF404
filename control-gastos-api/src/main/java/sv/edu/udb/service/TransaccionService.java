TransaccionService.java


package sv.edu.udb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.TransaccionRequestDTO;
import sv.edu.udb.dto.TransaccionResponseDTO;
import sv.edu.udb.event.TransaccionCreadaEvent;
import sv.edu.udb.event.TransaccionEliminadaEvent;
import sv.edu.udb.exception.ResourceNotFoundException;
import sv.edu.udb.model.Categoria;
import sv.edu.udb.model.Transaccion;
import sv.edu.udb.repository.CategoriaRepository;
import sv.edu.udb.repository.TransaccionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CategoriaRepository categoriaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransaccionResponseDTO registrarTransaccion(TransaccionRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + dto.getCategoriaId()));

        Transaccion transaccion = Transaccion.builder()
                .descripcion(dto.getDescripcion())
                .monto(dto.getMonto())
                .fecha(LocalDateTime.now())
                .categoria(categoria)
                .build();

        Transaccion guardada = transaccionRepository.save(transaccion);
        log.info("Transacción guardada con ID={}", guardada.getId());

        // 🔔 Publicar evento
        eventPublisher.publishEvent(new TransaccionCreadaEvent(this, guardada));

        return mapToDTO(guardada);
    }

    public List<TransaccionResponseDTO> listarTransacciones() {
        return transaccionRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public TransaccionResponseDTO obtenerPorId(Long id) {
        Transaccion t = transaccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transacción no encontrada con id: " + id));
        return mapToDTO(t);
    }

    public BigDecimal calcularBalance() {
        BigDecimal ingresos = transaccionRepository.sumMontoByTipo("INGRESO");
        BigDecimal gastos = transaccionRepository.sumMontoByTipo("GASTO");
        return ingresos.subtract(gastos);
    }

    public List<TransaccionResponseDTO> listarPorCategoria(Long categoriaId) {
        return transaccionRepository.findByCategoriaId(categoriaId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public void eliminarTransaccion(Long id) {
        if (!transaccionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transacción no encontrada con id: " + id);
        }
        transaccionRepository.deleteById(id);
        eventPublisher.publishEvent(new TransaccionEliminadaEvent(this, id));
    }

    private TransaccionResponseDTO mapToDTO(Transaccion t) {
        return TransaccionResponseDTO.builder()
                .id(t.getId())
                .descripcion(t.getDescripcion())
                .monto(t.getMonto())
                .fecha(t.getFecha())
                .categoriaId(t.getCategoria().getId())
                .categoriaNombre(t.getCategoria().getNombre())
                .build();
    }
}
