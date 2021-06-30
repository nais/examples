# api-protected-springsecurity

Eksempel på en Spring Boot applikasjon med REST APIer som krever OAuth 2.0 Bearer `access_tokens` fra Azure AD. Eksemplet har som mål å vise hvordan dette kan gjøres og bør ikke antas å være en fullverdig mal for en applikasjon i produksjon.  

Token validering gjøres ved hjelp av `Spring Security` og `spring-boot-starter-oauth2-resource-server`. Det er laget en ekstra `OAuth2TokenValidator` for å kunne validere audience (`aud`i token)
som er sentralt i en OAuth 2.0 arkitektur for å verifisere at tokenet som sendes er ment for dette APIet.

Applikasjonen er stateless og har ikke noe forhold til browser (som f.eks. i OpenID Connect), men forventer et OAuth 2.0 Bearer `access_token` i JWT format i `Authorization` HTTP header.

## Konfigurasjon

### Bygge

`../../../gradlew build`

### Kjøre lokalt

#### docker-compose
Kan kjøres opp sammen med en login proxy applikasjon og et annet beskyttet "middletier" API for ende til ende test med oppsettet beskrevet [her](../../docker)

#### IDE
Trykk run på klassen `src/main/java/no/nav/security/examples/springsecurity/ApiProtectedSpringSecurityApplication.java`

Applikasjonen vil da starte opp på port `8080`

### Reference Documentation

* [Spring Web Starter](https://docs.spring.io/spring-boot/docs/{bootVersion}/reference/htmlsingle/#boot-features-developing-web-applications)
* [OAuth2 Resource Server](https://docs.spring.io/spring-boot/docs/{bootVersion}/reference/htmlsingle/#boot-features-security-oauth2-server)
