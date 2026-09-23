package sv.edu.udb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.model.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // Consulta para obtener transacciones por categoría
    List<Transaccion> findByCategoriaId(Long categoriaId);

    // Consulta para obtener transacciones en un rango de fechas
    List<Transaccion> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Consulta JPQL para calcular el balance total (ingresos - gastos)
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t WHERE t.categoria.tipo = :tipo")
    BigDecimal sumMontoByTipo(@Param("tipo") String tipo);
}
