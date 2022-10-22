package com.xxxx.cms.dao;

import com.xxxx.cms.base.BaseMapper;
import com.xxxx.cms.vo.Roles;

import java.util.List;
import java.util.Map;

public interface RolesMapper extends BaseMapper<Roles,Integer> {

    public List<Map<String,Object>> queryAllRoles(Integer userId);

    public Roles  selectByRoleName(String roleName);


}