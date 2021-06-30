# login-proxy-nodejs
Eksempel på en barebones [Express](https://expressjs.com/)-applikasjon som håndterer innlogging med bruk av OpenID Connect 
og authorization code flow for å autentisere brukeren og få tak i et access token via on-behalf-of-flyten som kan brukes 
mot andre APIer.

Denne løsningen er en reverse proxy som er tenkt brukt mellom en typisk frontendapplikasjon og et mottakende, 
sikret backend API. Vi kjører altså opp en Node.js-server som håndterer det meste av tokenflyten og "API-proxying", 
dvs. legger på et access token i Authorization-headeren for alle kall brukeren gjør fra frontenden til backenden.

## Hvordan komme igang

`yarn` eller `yarn install` for å installere dependencies.

### Konfigurasjon

Applikasjonen bruker [dotenv](https://github.com/motdotla/dotenv) for å laste inn miljøvariabler. 

Se [./src/config.js](./src/config.js) for hvilke variabler som forventes satt. 
Se [konfigurasjon av reverse-proxy](#reverse-proxy) for konfigurasjon av denne.

Kan også konfigureres til å bruke en proxy for alle kall mot Azure AD, 
se [./src/proxy/http-proxy](src/proxy/http-proxy.js) for detaljer.

### Kjøring

`yarn start` for å starte serveren lokalt (default http://localhost:3000)

## Endepunkter

Referer til [./src/routes.js](./src/routes.js) for en komplett beskrivelse av endepunktene som er tilgjengelige.

- [/](http://localhost:3000/) - viser tokeninfo for den innloggede brukeren
- [/me](http://localhost:3000/me) - viser informasjon (bl.a. NAV-ident) om den innloggede brukeren via kall til Microsoft Graph API-et.

**NB: I produksjon bør ikke responsene i de overnevnte rutene eksponeres. De ligger kun i dette eksempelet for læringens skyld.**

- [/{api.path}/*](http://localhost:3000/{api.path}/*) - proxyer alle endepunkter som matcher denne pathen til det 
konfigurerte downstream APIet med automatisk bruk av on-behalf-of flyt for å få tak i korrekt access token.
Se [reverse-proxy](#reverse-proxy) for detaljer.

## Reverse-proxy 

### Konfigurasjon 
Konfigurasjon for å videresende kall til et downstream API.

- `DOWNSTREAM_API_CLIENT_ID`
- `DOWNSTREAM_API_URL`
- `DOWNSTREAM_API_PATH` - optional, default er `downstream`

### Bruk

Gitt
```
DOWNSTREAM_API_URL = https://backend.app/
```

og at denne proxyen er eksponert på `https://frontend.app/`, så kan alle requests fra en frontendapplikasjon bli proxyet 
ved å kalle gjøre kall til

```
https://frontend.app/<DOWNSTREAM_API_PATH>/*
```

Eksempel (med `DOWNSTREAM_API_PATH=downstream`):

```
https://frontend.app/downstream/ping -> https://backend.app/ping
```

### Selvsignerte sertifikater
Applikasjoner i NAV (i hvert fall internt) terminerer SSL med et selvsignert sertifikat.
Det vil si at hvis du forsøker å proxye kall til f.eks. `https://app-b.nais.preprod.local` så vil du sannsynligvis ende opp med denne feilen:

`Error: self signed certificate in certificate chain`

Det er flere løsninger her:

#### 1 (anbefalt)
Ideelt sett kjører både denne proxyen og downstreamapplikasjonen på NAIS i samme cluster. 
URLen i eksempelet ovenfor er en _ingress_ som egentlig eksponerer applikasjonen eksternt ut fra clusteret.
Innad i clusteret kan man kommunisere direkte mellom applikasjoner ved å bruke [service discovery](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/).

Kort forklart kan man i stedet for `https://app-b.nais.preprod.local` heller bruke `http://app-b.svc.nais.local`

#### 2
Om service discovery ikke er et alternativ så kan man sette følgende miljøvariabel for å få Node til å stole på NAVs sertifikater:

```
NODE_EXTRA_CA_CERTS=/etc/ssl/ca-bundle.pem
```

der `/etc/ssl/ca-bundle.pem` inneholder NAVs sertifikater (denne filen er automatisk tilgjengelig om man kjører på NAIS).


## Statiske filer
Om ønskelig kan man også serve statiske filer (e.g. en frontendapp i ditt favorittrammeverk) sammen med denne proxyen, 
siden den tross alt er en helt vanlig Node-server. 

En fiktiv "applikasjon" ligger under [./website](./website) og serves på `/*`, dvs. alle pather utenom rutene som er definert ovenfor.

## Sesjonshåndtering
Tokens eksponeres ikke mot brukere/browser, men blir i stedet håndtert på server-siden. 

Default for lokalt oppsett er å bruke en in-memory store for lagring av sesjoner. 
I et produksjonsmiljø derimot anbefales det å bruke Redis til å persistere sesjoner. 
Spesielt gjelder det om man har flere instanser av applikasjonen kjørende.

Miljøvariabelen `SESSION_KEY` bør settes til en tilfeldig streng av betydelig lengde. 
Denne brukes til å signere cookiesene som brukes til å lagre session ID for brukeren i nettleseren.

### Redis
For oppsett av Redis på NAIS så refereres det til dokumentasjon på [NAIS](https://doc.nais.io/persistence/redis#secure-redis).

Applikasjonen kjører opp en in-memory store dersom `NODE_ENV === 'development'`. 

Ellers vil det forsøkes en tilkobling til Redis som kan konfigureres med følgende miljøvariabler:

- `REDIS_HOST`
- `REDIS_PORT` (default 6379)
- `REDIS_PASSWORD`
