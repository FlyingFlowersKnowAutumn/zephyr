CREATE TABLE sys_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO sys_role (id, name, description) VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'ADMIN', '系统管理员'),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'USER', '普通用户');

CREATE TABLE sys_user_role (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE sys_permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('MENU', 'BUTTON', 'API')),
    parent_id UUID REFERENCES sys_permission(id) ON DELETE SET NULL,
    path VARCHAR(200),
    icon VARCHAR(100),
    sort_order INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' CHECK (status IN ('ENABLED', 'DISABLED')),
    description VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_sys_permission_code ON sys_permission(code);
CREATE INDEX idx_sys_permission_parent_id ON sys_permission(parent_id);

INSERT INTO sys_permission (id, name, code, type, sort_order, description) VALUES
    ('a1a2b3c4-d5e6-7890-abcd-ef1234567890', '文章管理', 'article:manage', 'MENU', 1, '文章管理菜单'),
    ('a2b3c4d5-e6f7-8901-bcde-f12345678901', '发布文章', 'article:publish', 'BUTTON', 2, '发布文章权限'),
    ('a3c4d5e6-f7a8-9012-cdef-f12345678902', '删除文章', 'article:delete', 'BUTTON', 3, '删除文章权限'),
    ('a4d5e6f7-a8b9-0123-def0-f12345678903', '用户管理', 'user:manage', 'MENU', 4, '用户管理菜单'),
    ('a5e6f7a8-b9c0-1234-ef01-f12345678904', '评论管理', 'comment:manage', 'MENU', 5, '评论管理菜单'),
    ('a6f7a8b9-c0d1-2345-f012-f12345678905', '留言管理', 'message:manage', 'MENU', 6, '留言管理菜单'),
    ('a7a8b9c0-d1e2-3456-f012-f12345678906', '修改用户名', 'user:update-username', 'BUTTON', 7, '修改用户名权限'),
    ('a8b9c0d1-e2f3-4567-0123-f12345678907', '修改头像', 'user:update-avatar', 'BUTTON', 8, '修改头像权限'),
    ('a9c0d1e2-f3a4-5678-1234-f12345678908', '修改密码', 'user:update-password', 'BUTTON', 9, '修改密码权限'),
    ('a0d1e2f3-a4b5-6789-2345-f12345678909', '修改邮箱', 'user:update-email', 'BUTTON', 10, '修改邮箱权限'),
    ('ab1c2d3e-f4c5-6789-0abc-def234567890', '修改签名', 'user:update-bio', 'BUTTON', 11, '修改签名权限（仅管理员）');

CREATE TABLE sys_role_permission (
    role_id UUID NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES sys_permission(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.name = 'ADMIN';

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.name = 'USER' AND p.code IN ('comment:manage', 'message:manage', 'user:update-username', 'user:update-avatar', 'user:update-password', 'user:update-email');

CREATE TABLE sys_login_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(50),
    ip VARCHAR(50),
    location VARCHAR(200),
    device VARCHAR(100),
    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAIL')),
    message VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_login_log_user_id ON sys_login_log(user_id);
CREATE INDEX idx_login_log_created_at ON sys_login_log(created_at);
