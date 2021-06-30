# api-onbehalfof-springsecurity

Eksempel på en Spring Boot applikasjon med REST APIer som krever OAuth 2.0 Bearer `access_tokens` fra Azure AD og som gjør videre kall mot andre APIer. Eksemplet har som mål å vise hvordan dette kan gjøres og bør ikke antas å være en fullverdig mal for en applikasjon i produksjon.  

Token validering gjøres ved hjelp av `Spring Security` og `spring-boot-starter-oauth2-resource-server`. Det er laget en ekstra `OAuth2TokenValidator` for å kunne validere audience (`aud`i token)
som er sentralt i en OAuth 2.0 arkitektur for å verifisere at tokenet som sendes er ment for dette APIet.

Applikasjonen er stateless og har ikke noe forhold til browser (som f.eks. i OpenID Connect), men forventer et OAuth 2.0 Bearer `access_token` i JWT format i `Authorization` HTTP header.

Ved kall "downstream" dvs. mot andre APIer er det implementert `OAuth 2.0 On-Behalf-Of` flow (https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow) -  
som kort forklart går ut på å veksle inn `access_token` man mottar med et nytt `access_token` som er ment for APIet man skal kalle, mens man fortsatt beholder subjektet i tokenet (`sub` claim) 

Spring Security har ikke støtte for `OAuth 2.0 On-Behalf-Of` så det er laget en implementasjon av en klient for denne flyten som kan "plugges" inn i Springs OAuth 2.0 mekanismer.

## Konfigurasjon

### Bygge

`../../../gradlew build`

### Kjøre lokalt

#### docker-compose
Kan kjøres opp sammen med en login proxy applikasjon og et annet beskyttet "downstream" API for ende til ende test med oppsettet beskrevet [her](../../docker)

#### IDE

Filen `src/main/resources/application-local.secrets.properties` inneholder en `client_id` og `client_secret` som er gyldig for Azure AD i `dev`, 
den er imidlertid kryptert så om du ikke har tilgang til å dekryptere må du hente verdiene fra [Vault](https://vault.adeo.no/ui/vault/secrets/azuread/show/dev/creds/security-blueprint-client) 
og legge de til i fila.

- Legg inn `AZURE_APP_CLIENT_ID` og `AZURE_APP_CLIENT_SECRET` i  `src/main/resources/application-local.secrets.properties`

```
AZURE_APP_CLIENT_ID=123456789
AZURE_APP_CLIENT_SECRET=passord
```


- Trykk run på klassen `src/test/java/no/nav/security/examples/springsecurity/OnBehalfOfMockApplication.java`


Applikasjonen vil da starte opp på port `8080` sammen med en tilhørende mock av et "downstream" API på port `1111`.

### Reference Documentation

* [Spring Web Starter](https://docs.spring.io/spring-boot/docs/{bootVersion}/reference/htmlsingle/#boot-features-developing-web-applications)
* [OAuth2 Resource Server](https://docs.spring.io/spring-boot/docs/{bootVersion}/reference/htmlsingle/#boot-features-security-oauth2-server)
