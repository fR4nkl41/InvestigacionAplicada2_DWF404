TransaccionCreadaEvent.java:

package sv.edu.udb.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import sv.edu.udb.model.Transaccion;

import java.time.LocalDateTime;

/**
 * Evento que se publica cuando se registra una nueva transacción.
 * Extiende ApplicationEvent para integrarse con el mecanismo
 * de eventos internos de Spring.
 */
@Getter
public class TransaccionCreadaEvent extends ApplicationEvent {

    private final Transaccion transaccion;
    private final LocalDateTime fechaEvento;

    public TransaccionCreadaEvent(Object source, Transaccion transaccion) {
        super(source);
        this.transaccion = transaccion;
        this.fechaEvento = LocalDateTime.now();
    }
}
