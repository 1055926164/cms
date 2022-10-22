package com.xxxx.cms.dao;


import com.xxxx.cms.base.BaseMapper;
import com.xxxx.cms.vo.UserRoles;

public interface UserRolesMapper extends BaseMapper<UserRoles,Integer> {

    public Integer countUserRoleByUserId(Integer userId);

    public Integer  deleteUserRoleByUserId(Integer userId);

    public int  countUserRoleByRoleId(Integer roleId);

    public int   deleteUserRoleByRoleId(Integer roleId);

}