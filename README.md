# My Application README

Tässä on minun Java Web kurssin viimeinen projekti, joka on pieni web-sovellus.

# EventApp – Tapahtumanvarausjärjestelmä

Vaadin 25 + Spring Boot 4 harjoitustyö.

## Teknologiat
- Java 21
- Spring Boot 4.0.6
- Vaadin 25.1.5
- H2 / PostgreSQL
- Spring Security 7
- Hibernate Envers

## Käynnistys

### Kehitysympäristö
```bash
./mvnw spring-boot:run
```
Avaa selaimessa: http://localhost:8080

Tunnukset:
- admin / admin123 (ADMIN-rooli)
- user / user123 (USER-rooli)

### Docker
```bash
docker-compose up --build
```

## Ominaisuudet
- CRUD-toiminnot neljälle entiteetille
- Criteria API -haku
- Spring Security + roolit
- Lokalisointi (FI/EN)
- Server Push
- CSV-tuonti ja vienti
- Sähköposti-ilmoitukset
- Salasanan vaihto
- Muutoshistoria (Envers)