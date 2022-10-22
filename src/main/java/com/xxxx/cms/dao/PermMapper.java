package com.xxxx.cms.dao;

import com.xxxx.cms.base.BaseMapper;
import com.xxxx.cms.vo.Perm;

import java.util.List;

public interface PermMapper extends BaseMapper<Perm,Integer> {


    public int  countPermissionByRoleId(Integer roleId);

    public int  deletePermissionByRoleId(Integer roleId);

    public List<Integer> queryRoleHasAllMids(Integer roleId);

    List<String>  queryUserHasRoleIdsHasModuleIds(Integer userId);


    Integer  countPermissionByModuleId(Integer mid);

    int  deletePermissionByModuleId(Integer mid);
}