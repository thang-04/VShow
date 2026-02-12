#!/bin/bash

# Script redeploy cho VShow (Backend) và Vshow-Ui (Frontend)
# Phien ban toi uu: Chi rebuild va restart nhung service co thay doi.

# Kiem tra thu muc
if [ ! -d "./VShow" ] || [ ! -d "./Vshow-Ui" ]; then
    echo "Loi: Khong tim thay thu muc VShow hoac Vshow-Ui."
    exit 1
fi

# Tu dong phat hien lenh docker-compose
if docker compose version &>/dev/null; then
    DOCKER_COMPOSE_CMD="docker compose"
elif command -v docker-compose &>/dev/null; then
    DOCKER_COMPOSE_CMD="docker-compose"
else
    echo "Loi: Khong tim thay 'docker compose' hoac 'docker-compose'."
    exit 1
fi

echo "=================================================="
echo "Bat dau qua trinh Cap nhat Code moi..."
echo "Su dung lenh: $DOCKER_COMPOSE_CMD"
echo "=================================================="

# 1. Update Backend (VShow)
# Luu y: Backend chua Database, nen dung 'up -d --build' de tranh restart DB neu khong can thiet.
echo "Dang kiem tra va cap nhat Backend (VShow)..."
cd ./VShow || exit
$DOCKER_COMPOSE_CMD up -d --build --remove-orphans
cd ..

# 2. Update Frontend (Vshow-Ui)
# Frontend thuong hay bi loi Conflict ten container container neu chay 'up' truc tiep tu context khac.
# De tranh loi nay, ta xoa thang container bang lenh docker rm (khong dung compose rm vi co the no khong tim thay container trong context hien tai).
echo "Dang kiem tra va cap nhat Frontend (Vshow-Ui)..."

# Xoa container cu (neu co) de tranh conflict
echo "Dang xoa container vshow-ui cu..."
docker rm -f vshow-ui > /dev/null 2>&1 || true

cd ./Vshow-Ui || exit
$DOCKER_COMPOSE_CMD up -d --build
cd ..

echo "=================================================="
echo "Cap nhat Hoan Tat!"
echo "=================================================="
