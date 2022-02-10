# service-to-service

Inneholder eksempler på "maskin-til-maskin" kommunikasjon med OAuth 2.0, herunder:
- [OAuth 2.0 On Behalf Of Flow: API kall på vegne av innlogget sluttbruker](https://security.labs.nais.io/pages/guide/api-kall/sluttbruker/azure-ad.html)
- [OAuth 2.0 Client Credentials Flow: API kall uten innlogget sluttbruker](https://security.labs.nais.io/pages/guide/api-kall/maskin_til_maskin_uten_bruker.html)
- [OAuth 2.0 JWT Bearer tokens: API med tokenvalidering](https://security.labs.nais.io/pages/guide/token_validering.html)

Det er laget eksempler i et utvalg språk og rammeverk som har innebygget støtte for OAuth 2.0 (både som klient og "resourceserver"):
- Kotlin / [ktor](https://ktor.io/servers/index.html)
- Java / [Spring Boot med Spring Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2)
- Java / [Spring Boot med token-support](https://github.com/navikt/token-support)  

## Eksempelapplikasjoner

APIer som validerer JWT bearer `access_token` fra Azure AD: 

* [api-protected-ktor](api-protected-ktor)
* [api-protected-springsecurity](api-protected-springsecurity)
* [api-protected-tokensupport](api-protected-tokensupport)

APIer som validerer JWT bearer `access_token` fra Azure AD og 
kaller et annet API med et nytt token ved bruk av `OAuth 2.0 On Behalf Of flow`:

* [api-onbehalfof-ktor](api-onbehalfof-ktor)
* [api-onbehalfof-springsecurity](api-onbehalfof-springsecurity)
* [api-onbehalfof-tokensupport](api-onbehalfof-tokensupport)

"daemons" (i dette tilfellet enkle cli apper) som benytter `OAuth 2.0 Client Credentials` for å skaffe et `access_token` for å kalle et API:

* [daemon-clientcredentials-springsecurity](daemon-clientcredentials-springsecurity)
* [daemon-clientcredentials-tokensupport](daemon-clientcredentials-tokensupport)
