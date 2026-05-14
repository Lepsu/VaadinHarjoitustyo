package com.example.venue;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nimi on pakollinen")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Osoite on pakollinen")
    private String address;

    @NotBlank(message = "Kaupunki on pakollinen")
    private String city;

    @Min(value = 1, message = "Kapasiteetti oltava vähintään 1")
    private int capacity;

    @Email(message = "Virheellinen sähköpostiosoite")
    @NotBlank(message = "Sähköposti on pakollinen")
    private String contactEmail;

    @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Virheellinen puhelinnumero")
    private String phone;

    // Getterit ja setterit
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() { return name + " (" + city + ")"; }
}