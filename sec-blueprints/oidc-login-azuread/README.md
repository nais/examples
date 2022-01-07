# oidc-login-azuread

Eksemplene i [service-to-service](../service-to-service) er backendapplikasjoner og inneholder verken state eller interagerer
direkte med en bruker eller nettleser på noe vis. Endepunktene er dog sikret, og krever at enhver kallende applikasjon kan autorisere seg 
med et OAuth 2.0 Bearer `access_token` i JWT-format i `Authorization`-headeren.

En typisk frontendapplikasjon vil måtte få tak i nevnte token på vegne av en innlogget bruker på et eller annet vis 
og bruke dette i kallet mot den sikrede backenden. 

Denne mappen inneholder et par eksempler som tar for seg prosessen med å skaffe et slikt token.

Eksemplene under implementerer en reverse proxy tar seg av innlogging og tokens, separert fra en typisk backendapplikasjon som eksponerer
API-endepunkter - og videresender autentiserte forespørsler til en backend med et korrekt `access_token` lagt til 
gjennom bruk av on-behalf-of-flyten.

- [login-proxy-nodejs](./login-proxy-nodejs)
- [login-proxy-springcloud](./login-proxy-springcloud)
