package com.example.category;

import com.example.event.Event;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.envers.Audited;

import java.util.HashSet;
import java.util.Set;

@Entity
@Audited
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nimi on pakollinen")
    @Size(min = 2, max = 50)
    @Column(unique = true)
    private String name;

    @Size(max = 200)
    private String description;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$",
            message = "Värikoodi muodossa #RRGGBB")
    private String colorCode;

    @ManyToMany(mappedBy = "categories")
    private Set<Event> events = new HashSet<>();

    // Getterit ja setterit
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }

    public Set<Event> getEvents() { return events; }
    public void setEvents(Set<Event> events) { this.events = events; }

    @Override
    public String toString() { return name; }
}

