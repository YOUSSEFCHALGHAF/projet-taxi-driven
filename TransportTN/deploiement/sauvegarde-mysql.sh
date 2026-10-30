#!/usr/bin/env bash
# Sauvegarde de la base dans sauvegardes/transporttn-AAAAMMJJ-HHMM.sql.gz
# Exemple de tâche planifiée (crontab -e) : tous les jours à 3 h
#   0 3 * * * /chemin/vers/transporttn/deploiement/sauvegarde-mysql.sh
set -euo pipefail
cd "$(dirname "$0")/.."
set -a
# shellcheck disable=SC1091
source .env
set +a

mkdir -p sauvegardes
FICHIER="sauvegardes/transporttn-$(date +%Y%m%d-%H%M).sql.gz"
docker compose -f docker-compose.prod.yml exec -T mysql \
  sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines transporttn' \
  | gzip > "$FICHIER"

# On garde les 14 dernières sauvegardes
find sauvegardes -name 'transporttn-*.sql.gz' -type f | sort | head -n -14 | xargs -r rm --
echo "Sauvegarde créée : $FICHIER"