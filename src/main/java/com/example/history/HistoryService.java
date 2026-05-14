package com.example.history;

import com.example.event.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HistoryService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<HistoryEntry> getEventHistory(Long eventId) {
        List<HistoryEntry> entries = new ArrayList<>();

        AuditReader reader = AuditReaderFactory.get(entityManager);

        List<Object[]> revisions = reader.createQuery()
                .forRevisionsOfEntity(Event.class, false, true)
                .add(AuditEntity.id().eq(eventId))
                .getResultList();

        for (Object[] rev : revisions) {
            Event state = (Event) rev[0];
            org.hibernate.envers.DefaultRevisionEntity revEntity =
                    (org.hibernate.envers.DefaultRevisionEntity) rev[1];
            org.hibernate.envers.RevisionType revType =
                    (org.hibernate.envers.RevisionType) rev[2];

            LocalDateTime time = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(
                            revEntity.getRevisionDate().getTime()),
                    ZoneId.systemDefault());

            entries.add(new HistoryEntry(
                    revEntity.getId(),
                    time,
                    revType.name(),
                    state.getName() != null ? state.getName() : "-",
                    state.getDescription() != null
                            ? state.getDescription() : "-",
                    state.getCreatedBy() != null
                            ? state.getCreatedBy() : "system"
            ));
        }

        return entries;
    }
}