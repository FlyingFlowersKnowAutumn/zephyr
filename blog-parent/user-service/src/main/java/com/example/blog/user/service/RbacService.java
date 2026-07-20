package com.example.blog.user.service;

import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.entity.*;
import com.example.blog.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbacService {

    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final SysLoginLogRepository loginLogRepository;

    public SysRole getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> BusinessException.notFound("角色不存在"));
    }

    @Transactional
    public void assignRoleToUser(UUID userId, String roleName) {
        SysRole role = getRoleByName(roleName);
        
        SysUserRole userRole = SysUserRole.builder()
                .userId(userId)
                .roleId(role.getId())
                .build();
        
        userRoleRepository.save(userRole);
        log.info("Assigned role {} to user {}", roleName, userId);
    }

    @Transactional
    public void removeRoleFromUser(UUID userId, String roleName) {
        SysRole role = getRoleByName(roleName);
        
        SysUserRoleId id = new SysUserRoleId(userId, role.getId());
        userRoleRepository.deleteById(id);
        log.info("Removed role {} from user {}", roleName, userId);
    }

    public Set<String> getUserRoles(UUID userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId)
                .map(roleRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(SysRole::getName)
                .collect(Collectors.toSet());
    }

    public Set<String> getUserPermissions(UUID userId) {
        Set<UUID> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toSet());

        return roleIds.stream()
                .flatMap(roleId -> rolePermissionRepository.findByRoleId(roleId).stream())
                .map(SysRolePermission::getPermissionId)
                .map(permissionRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .filter(p -> "ENABLED".equals(p.getStatus()))
                .map(SysPermission::getCode)
                .collect(Collectors.toSet());
    }

    public boolean hasPermission(UUID userId, String permissionCode) {
        return getUserPermissions(userId).contains(permissionCode);
    }

    public boolean isAdmin(UUID userId) {
        return getUserRoles(userId).contains("ADMIN");
    }

    @Transactional
    public void recordLoginLog(UUID userId, String username, String ip, String device, boolean success, String message) {
        SysLoginLog logEntry = SysLoginLog.builder()
                .userId(userId)
                .username(username)
                .ip(ip)
                .device(device)
                .status(success ? "SUCCESS" : "FAIL")
                .message(message)
                .build();
        
        loginLogRepository.save(logEntry);
    }

    public List<SysLoginLog> getLoginLogsByUser(UUID userId) {
        return loginLogRepository.findByUserId(userId);
    }

    public List<SysPermission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public List<SysRole> getAllRoles() {
        return roleRepository.findAll();
    }
}