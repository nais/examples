# api-protected-tokensupport

Eksempel på en Spring Boot applikasjon med REST APIer som krever OAuth 2.0 Bearer `access_tokens` fra Azure AD 
og som gjør videre kall mot andre APIer. Eksemplet har som mål å vise hvordan dette kan gjøres og bør ikke antas å være en fullverdig mal for en applikasjon i produksjon.  

Token validering gjøres ved hjelp av [`token-support`](https://github.com/navikt/token-support) og spesifikt modulen `token-validation-spring`. 
`token-validation-spring` tilbyr funksjonalitet for token validering i Spring Boot apper uten Spring Security ved hjelp av wrappere rundt biblioteket [Nimbus](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk). 

Applikasjonen er stateless og har ikke noe forhold til browser (som f.eks. i OpenID Connect), men forventer et OAuth 2.0 Bearer `access_token` i JWT format i `Authorization` HTTP header.

Ved kall "downstream" dvs. mot andre APIer er det implementert `OAuth 2.0 On-Behalf-Of` flow (https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow) -  
som kort forklart går ut på å veksle inn `access_token` man mottar med et nytt `access_token` som er ment for APIet man skal kalle, mens man fortsatt beholder subjektet i tokenet (`sub` claim)

[`token-support`](https://github.com/navikt/token-support) har full støtte for `OAuth 2.0 On-Behalf-Of` via modulen `token-client-spring`. 
                                   

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


- Trykk run på klassen `src/test/java/no/nav/security/examples/tokensupport/OnBehalfOfMockApplication.java`


Applikasjonen vil da starte opp på port `8080` sammen med en tilhørende mock av et "downstream" API på port `1111`.


