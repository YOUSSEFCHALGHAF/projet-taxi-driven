#!/usr/bin/env bash
# =============================================================
# Obtient le premier certificat HTTPS Let's Encrypt (à lancer UNE fois sur le serveur).
# Prérequis : le domaine pointe vers ce serveur, les ports 80 et 443 sont ouverts.
#
#   ./deploiement/init-letsencrypt.sh            # certificat réel
#   STAGING=1 ./deploiement/init-letsencrypt.sh  # certificat de test (limites Let's Encrypt plus souples)
# =============================================================
set -euo pipefail
cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "Fichier .env introuvable : copiez .env.exemple en .env et remplissez-le." >&2
  exit 1
fi
set -a
# shellcheck disable=SC1091
source .env
set +a

: "${DOMAINE:?DOMAINE manquant dans .env}"
: "${EMAIL_LETSENCRYPT:?EMAIL_LETSENCRYPT manquant dans .env}"

if [ "$DOMAINE" = "localhost" ]; then
  echo "Let's Encrypt ne délivre pas de certificat pour localhost : le site reste en HTTP." >&2
  exit 1
fi

COMPOSE=(docker compose -f docker-compose.prod.yml)
OPTION_TEST=()
if [ -n "${STAGING:-}" ]; then
  OPTION_TEST=(--staging)
fi

echo "1/3 Démarrage de l'application en HTTP…"
"${COMPOSE[@]}" up -d --build nginx

echo "2/3 Demande du certificat pour ${DOMAINE}…"
"${COMPOSE[@]}" run --rm --entrypoint certbot certbot certonly \
  --webroot -w /var/www/certbot \
  -d "$DOMAINE" \
  --email "$EMAIL_LETSENCRYPT" \
  --agree-tos --no-eff-email \
  "${OPTION_TEST[@]}"

echo "3/3 Passage en HTTPS…"
"${COMPOSE[@]}" restart nginx
"${COMPOSE[@]}" up -d certbot

echo "Terminé : https://${DOMAINE}"