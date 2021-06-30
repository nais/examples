# Eksempler på OAuth 2.0 / OpenID Connect flows

Inneholder følgende mapper:

- [**docker**](docker): inneholder docker-compose filer for å kunne kjøre ende-til-ende tester på eksempelapplikasjonene 

- [**oidc-login-azuread**](oidc-login-azuread): inneholder eksempler på applikasjoner som tilbyr browser innlogging:
    - [OpenID Connect Authorization Code Flow: Innlogging / brukerautentisering i browser](https://security.labs.nais.io/pages/guide/innlogging/ansatte.html)

- [**service-to-service**](service-to-service): inneholder eksempler på "maskin-til-maskin" kommunikasjon med OAuth 2.0:
    - [OAuth 2.0 On Behalf Of Flow: API kall på vegne av innlogget sluttbruker](https://security.labs.nais.io/pages/guide/api-kall/sluttbruker/azure-ad.html)
    - [OAuth 2.0 Client Credentials Flow: API kall uten innlogget sluttbruker](https://security.labs.nais.io/pages/guide/api-kall/maskin_til_maskin_uten_bruker.html)
    - [OAuth 2.0 JWT Bearer tokens: API med tokenvalidering](https://security.labs.nais.io/pages/guide/token_validering.html)
