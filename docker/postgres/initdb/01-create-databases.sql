-- 仅在数据卷为空时由 postgres 镜像的 entrypoint 执行一次。
-- user_db 由 POSTGRES_DB 自动创建，这里只补另外两个业务库。
CREATE DATABASE article_db;
CREATE DATABASE interaction_db;
