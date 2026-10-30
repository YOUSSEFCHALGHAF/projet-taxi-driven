#!/bin/sh
# Exécuté automatiquement par l'image officielle Nginx avant le démarrage.
# Choisit la configuration HTTP ou HTTPS selon la présence du certificat Let's Encrypt.
set -eu

DOMAINE="${DOMAINE:-localhost}"
CERTIFICAT="/etc/letsencrypt/live/${DOMAINE}/fullchain.pem"

if [ -f "$CERTIFICAT" ]; then
    MODE=https
else
    MODE=http
fi

echo "TransportTN : configuration Nginx en ${MODE} pour ${DOMAINE}"
# Seule ${DOMAINE} est remplacée : les variables Nginx ($host, $uri…) restent intactes
# shellcheck disable=SC2016  # guillemets simples voulus : envsubst reçoit le nom littéral de la variable
envsubst '${DOMAINE}' < "/etc/nginx/modeles/${MODE}.conf.template" > /etc/nginx/conf.d/default.conf

# En HTTPS : rechargement toutes les 6 h pour prendre en compte les certificats renouvelés
if [ "$MODE" = "https" ]; then
    ( while :; do sleep 6h; nginx -s reload; done ) &
fi