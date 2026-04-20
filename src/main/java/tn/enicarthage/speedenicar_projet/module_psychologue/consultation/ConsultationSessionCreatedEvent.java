package tn.enicarthage.speedenicar_projet.module_psychologue.consultation;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ConsultationSessionCreatedEvent extends ApplicationEvent {

    private final ConsultationSession session;

    public ConsultationSessionCreatedEvent(Object source, ConsultationSession session) {
        super(source);
        this.session = session;
    }
}
