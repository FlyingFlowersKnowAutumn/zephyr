#!/usr/bin/env bash
# 构建一切产物：后端 jar、前端 dist、docker 镜像
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> 1/3 后端打包"
(cd blog-parent && mvn -B clean package -DskipTests)

echo "==> 2/3 前端构建"
(cd blog-frontend && npm ci && npm run build)

echo "==> 3/3 构建镜像"
docker compose build

echo "构建完成。启动：docker compose up -d"
