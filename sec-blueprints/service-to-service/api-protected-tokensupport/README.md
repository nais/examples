# api-protected-tokensupport

Eksempel på en Spring Boot applikasjon med REST APIer som krever OAuth 2.0 Bearer `access_tokens` fra Azure AD. 
Eksemplet har som mål å vise hvordan dette kan gjøres og bør ikke antas å være en fullverdig mal for en applikasjon i produksjon.  

Token validering gjøres ved hjelp av [`token-support`](https://github.com/navikt/token-support) og spesifikt modulen `token-validation-spring`. 
`token-validation-spring` tilbyr funksjonalitet for token validering i Spring Boot apper uten Spring Security ved hjelp av wrappere rundt biblioteket [Nimbus](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk). 

Applikasjonen er stateless og har ikke noe forhold til browser (som f.eks. i OpenID Connect), men forventer et OAuth 2.0 Bearer `access_token` i JWT format i `Authorization` HTTP header.
                       

## Konfigurasjon

### Bygge

`../../../gradlew build`

### Kjøre lokalt

#### docker-compose
Kan kjøres opp sammen med en login proxy applikasjon og et annet beskyttet "middletier" API for ende til ende test med oppsettet beskrevet [her](../../docker)

#### IDE

Trykk run på klassen `src/main/java/no/nav/security/examples/tokensupport/ApiProtectedTokenSupportApplication.java`


Applikasjonen vil da starte opp på port `8080`.


