package com.ahmet.tpm.models;

public class ProjectMember {

    private int projectMemberId;
    private Integer projectId;
    private Integer userId;
    private String roleInProject;

    public ProjectMember(){}

    public ProjectMember(int projectMemberId, Integer projectId, Integer userId, String roleInProject) {
        this.projectMemberId = projectMemberId;
        this.projectId = projectId;
        this.userId = userId;
        this.roleInProject = roleInProject;
    }

    public ProjectMember(Integer projectId, Integer userId, String roleInProject) {
        this.projectId = projectId;
        this.userId = userId;
        this.roleInProject = roleInProject;
    }

    public int getProjectMemberId() {
        return projectMemberId;
    }

    public void setProjectMemberId(int projectMemberId) {
        this.projectMemberId = projectMemberId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRoleInProject() {
        return roleInProject;
    }

    public void setRoleInProject(String roleInProject) {
        this.roleInProject = roleInProject;
    }
}
