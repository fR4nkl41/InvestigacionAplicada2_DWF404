TransaccionEventListener.java:

package sv.edu.udb.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Listener de eventos de transacciones.
 *
 * Demuestra el paradigma Event-Driven:
 *  - @EventListener: reacciona a eventos publicados en el contexto de Spring.
 *  - @Async: ejecuta el listener en un hilo separado (no bloquea al productor).
 */
@Component
@Slf4j
public class TransaccionEventListener {

    /**
     * Listener síncrono: se ejecuta en el mismo hilo que publica el evento.
     * Útil para operaciones rápidas de auditoría.
     */
    @EventListener
    public void onTransaccionCreada(TransaccionCreadaEvent event) {
        log.info("[EVENT-SYNC] Transacción creada ID={} monto={} categoría={}",
                event.getTransaccion().getId(),
                event.getTransaccion().getMonto(),
                event.getTransaccion().getCategoria().getNombre());
    }

    /**
     * Listener ASÍNCRONO: se ejecuta en un hilo del pool "async-event-*".
     * Simula el envío de notificación / correo sin bloquear la respuesta HTTP.
     */
    @Async
    @EventListener
    public void notificarTransaccionCreada(TransaccionCreadaEvent event) {
        String hilo = Thread.currentThread().getName();
        log.info("[EVENT-ASYNC] [{}] Iniciando notificación para transacción ID={}",
                hilo, event.getTransaccion().getId());

        try {
            // Simula el envío de un correo / llamada a servicio externo
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[EVENT-ASYNC] [{}] Notificación enviada para transacción ID={} (monto={})",
                hilo,
                event.getTransaccion().getId(),
                event.getTransaccion().getMonto());
    }

    /**
     * Listener que simula el recálculo del balance después de una eliminación.
     */
    @Async
    @EventListener
    public void recalcularBalanceTrasEliminacion(TransaccionEliminadaEvent event) {
        String hilo = Thread.currentThread().getName();
        log.info("[EVENT-ASYNC] [{}] Recalculando balance tras eliminar transacción ID={}",
                hilo, event.getTransaccionId());
    }
}
