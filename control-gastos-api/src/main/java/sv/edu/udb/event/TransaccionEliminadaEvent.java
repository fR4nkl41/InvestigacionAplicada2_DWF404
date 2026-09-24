TransaccionEliminadaEvent.java:

package sv.edu.udb.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Evento que se publica cuando se elimina una transacción.
 */
@Getter
public class TransaccionEliminadaEvent extends ApplicationEvent {

    private final Long transaccionId;
    private final LocalDateTime fechaEvento;

    public TransaccionEliminadaEvent(Object source, Long transaccionId) {
        super(source);
        this.transaccionId = transaccionId;
        this.fechaEvento = LocalDateTime.now();
    }
}
