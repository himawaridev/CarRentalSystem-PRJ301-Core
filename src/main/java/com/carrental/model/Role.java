package com.carrental.model;

/**
 * Maps to dbo.Roles table.
 */
public class Role {
    private int roleId;
    private String roleName;
    private String description;

    public Role() {}

    public Role(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Role{roleId=" + roleId + ", roleName='" + roleName + "'}";
    }
}
