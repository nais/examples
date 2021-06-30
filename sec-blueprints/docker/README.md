# Ende til ende test av applikasjoner med OAuth 2.0

Det er laget `docker-compose` filer for å kunne teste eksempelapplikasjonene og hvordan de henger sammen.

Kjør scriptet `./services.sh` for å bygge og starte en "kjede" av applikasjoner som henger sammen.

### Oppsett med `docker-compose` lokalt:

**Sett opp `client_id` og `client_secret` for applikasjonene:**

Dere kan overstyre `client_id` og `client_secret` i de ulike `docker-compose.*.yml`-filene med egne credentials.

Alternativt kan dere bruke defaultene som er satt opp fra før. Passordene for disse må imidlertid hentes fra Vault. 

Dette kan gjøres på en av to måter:

#### 1. Automatisk
Krever at du har:
- [Vault CLI](https://www.vaultproject.io/docs/install)
- [jq](https://stedolan.github.io/jq/)
- [NavTunnel](https://github.com/navikt/navtunnel) kjørende og koblet til

Kjør [./secrets/setup.sh](./secrets/setup.sh). 
Verifiser at [login.env](./secrets/login.env) og [obo.env](./secrets/obo.env) ligger i mappen [secrets/](./secrets)

#### 2. Manuelt

Om skriptet ovenfor ikke fungerer for deg så kan du opprette de samme filene manuelt:

1. Lag en tom fil kalt [login.env](./secrets/login.env) i mappen [secrets/](./secrets)
2. [Gå til Vault](https://vault.adeo.no/ui/vault/secrets/azuread/show/dev/creds/security-blueprint-login)
3. Kopier verdien som står ved `client_secret`
4. Lim inn i den tomme filen slik at den ser slik ut: 

```
AZURE_APP_CLIENT_SECRET=<verdien du kopierte fra Vault>
```

F.eks.:

```
AZURE_APP_CLIENT_SECRET=uefiu23rkjdfs8
```

1. Lag en tom fil kalt [obo.env](./secrets/obo.env) i mappen [secrets/](./secrets)
2. [Gå til Vault](https://vault.adeo.no/ui/vault/secrets/azuread/show/dev/creds/security-blueprint-client)
3. Gjenta steg 3 og 4 som ovenfor.

Merk at dere må logge inn med `@trygdeetaten.no`-bruker for default-appene som er satt opp.

#### Eksempel for å starte ktor applikasjonene:
`./services.sh ktor up`
#### Eksempel for å starte tokensupport applikasjonene:
`./services.sh tokensupport up`
#### Eksempel for å starte springsecurity applikasjonene:
`./services.sh springsecurity up`

#### Endepunkter og porter

Scriptet vil kjøre en gradle build, samt bygge og kjøre docker containerene via docker-compose. 
Alle applikasjonene er satt opp til å integrere med Azure AD i dev.

Applikasjonene vil starte på følgende porter:

- login-aad (login-proxy-nodejs): `3000`
- middletier-api (api-onbehalfof-[ ktor | springsecurity | tokensupport ]): `8080`
- downstream-api (api-protected-[ ktor | springsecurity | tokensupport ]): `8081`

**Endepunkter:**

- [http://localhost:3000](http://localhost:3000) -  trigger innlogging og viser tokeninformasjon for første app i verdikjeden

- [http://localhost:3000/downstream/api](http://localhost:3000/downstream/api) - kaller første "nedstrøms" API (`api-onbehalfof-[ ktor | springsecurity | tokensupport ]`), og viser responsen.

- [http://localhost:3000/downstream/api/tokeninfo](http://localhost:3000/downstream/api/tokeninfo) - kaller første "nedstrøms" API (`api-onbehalfof-[ ktor | springsecurity | tokensupport ]`), og viser informasjon om tokenet som er mottatt.

- [http://localhost:3000/downstream/api/downstream/api](http://localhost:3000/downstream/api/downstream/api) - kaller første "nedstrøms" API (`api-onbehalfof-[ ktor | springsecurity | tokensupport ]`), som igjen kaller neste API (`api-protected-[ ktor | springsecurity | tokensupport ]`), og viser responsen.

- [http://localhost:3000/downstream/api/downstream/api/tokeninfo](http://localhost:3000/downstream/api/downstream/api/tokeninfo) - kaller første "nedstrøms" API (`api-onbehalfof-[ ktor | springsecurity | tokensupport ]`)) som igjen kaller neste API (`api-protected-[ ktor | springsecurity | tokensupport ]`), og viser informasjon om tokenet som er mottatt i siste API.

