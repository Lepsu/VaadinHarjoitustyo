package com.example.search;

import com.example.category.Category;
import com.example.event.Event;
import com.example.venue.Venue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventSearchService {

    @PersistenceContext
    private EntityManager em;

    public List<Event> search(
            String nameOrDescription,   // OR-ehto: nimi TAI kuvaus
            LocalDate dateFrom,         // päivämääräväli alku
            LocalDate dateTo,           // päivämääräväli loppu
            String categoryName,        // JOIN kategorioihin
            String venueCityOrName      // JOIN venueen, OR: kaupunki TAI nimi
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> root = cq.from(Event.class);

        // JOINit relaatioihin
        Join<Event, Venue> venueJoin =
                root.join("venue", JoinType.LEFT);
        Join<Event, Category> categoryJoin =
                root.join("categories", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        // (X OR Y) AND Z -rakenne:
        // (nimi LIKE % OR kuvaus LIKE %) → OR-ehto tekstihaulle
        if (nameOrDescription != null && !nameOrDescription.isBlank()) {
            String pattern = "%" + nameOrDescription.toLowerCase() + "%";
            Predicate namePred = cb.like(
                    cb.lower(root.get("name")), pattern);
            Predicate descPred = cb.like(
                    cb.lower(root.get("description")), pattern);
            // OR-ehto 1: nimi TAI kuvaus
            predicates.add(cb.or(namePred, descPred));
        }

        // Päivämäärähaku – alkamisaika välillä dateFrom–dateTo
        if (dateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("startTime"),
                    dateFrom.atStartOfDay()));
        }
        if (dateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.get("startTime"),
                    dateTo.atTime(23, 59, 59)));
        }

        // JOIN kategorioihin – hae kategorian nimen perusteella
        if (categoryName != null && !categoryName.isBlank()) {
            predicates.add(cb.like(
                    cb.lower(categoryJoin.get("name")),
                    "%" + categoryName.toLowerCase() + "%"));
        }

        // JOIN venueen – OR-ehto 2: kaupunki TAI nimi
        if (venueCityOrName != null && !venueCityOrName.isBlank()) {
            String p = "%" + venueCityOrName.toLowerCase() + "%";
            Predicate cityPred = cb.like(
                    cb.lower(venueJoin.get("city")), p);
            Predicate venueNamePred = cb.like(
                    cb.lower(venueJoin.get("name")), p);
            // OR-ehto 2: venue-kaupunki TAI venue-nimi
            predicates.add(cb.or(cityPred, venueNamePred));
        }

        // Yhdistetään kaikki predikaatit AND-rakenteella
        // Lopullinen rakenne: (nimi OR kuvaus) AND päivämäärä
        //                     AND kategoria AND (city OR venueName)
        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);
        cq.orderBy(cb.asc(root.get("startTime")));

        return em.createQuery(cq).getResultList();
    }
}