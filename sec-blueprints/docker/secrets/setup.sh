#!/usr/bin/env bash
set -e

#######################################
# Requirements:
# - Vault binaries in path
# - jq
# - Naisdevice
#######################################

export VAULT_ADDR="https://vault.adeo.no"

VAULT_TOKEN=$(vault login -address=$VAULT_ADDR -method oidc -token-only)
export VAULT_TOKEN

LOGIN_AZURE_APP_JWKS=$(vault kv get -address=$VAULT_ADDR -format json -field data azuread/dev/creds/security-blueprint-login | jq '.jwk | fromjson | { keys: [.] }' --raw-output -c)
OBO_CLIENT_SECRET=$(vault kv get -address=$VAULT_ADDR -format json -field data azuread/dev/creds/security-blueprint-client | jq '.client_secret' --raw-output)

cat <<EOF > "${BASH_SOURCE%/*}/login.env"
AZURE_APP_JWKS=${LOGIN_AZURE_APP_JWKS}
EOF

cat <<EOF > "${BASH_SOURCE%/*}/obo.env"
AZURE_APP_CLIENT_SECRET=${OBO_CLIENT_SECRET}
EOF
