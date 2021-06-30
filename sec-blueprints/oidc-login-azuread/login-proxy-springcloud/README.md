# login-proxy-springcloud

Eksempel på en Spring Boot proxy applikasjon som krever OpenID Connect innlogging (`authorization_code_flow`) fra Azure AD og som videresender kall mot andre APIer med OAuth2 Bearer `access_token`. Eksemplet har som mål å vise hvordan dette kan gjøres og bør ikke antas å være en fullverdig mal for en applikasjon i produksjon.  

Ved kall "downstream" dvs. mot andre APIer er det implementert `OAuth 2.0 On-Behalf-Of` flow (https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow) -  
som kort forklart går ut på å veksle inn `access_token` man mottar med et nytt `access_token` som er ment for APIet man skal kalle, mens man fortsatt beholder subjektet i tokenet (`sub` claim) 
