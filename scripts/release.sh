#!/usr/bin/env bash
# 带测试门禁的发布脚本：测试 -> 类型检查 -> 构建 -> 备份 -> 启动 -> 健康检查
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> 测试门禁（后端）"
(cd blog-parent && mvn -B test) || { echo "后端测试未通过，中止发布"; exit 1; }

echo "==> 类型检查（前端）"
(cd blog-frontend && npx vue-tsc --noEmit) || { echo "前端类型检查未通过，中止发布"; exit 1; }

./scripts/build.sh

echo "==> 发布前备份"
./scripts/backup.sh

echo "==> 启动"
docker compose up -d

echo "==> 健康检查"
sleep 60
for svc in blog-gateway blog-article blog-user blog-interaction blog-nginx; do
  status=$(docker inspect "$svc" --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}no-healthcheck{{end}}' 2>/dev/null || echo "missing")
  echo "  $svc -> $status"
done
