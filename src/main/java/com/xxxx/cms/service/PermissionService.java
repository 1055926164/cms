package com.xxxx.cms.service;


import com.xxxx.cms.base.BaseService;
import com.xxxx.cms.dao.PermMapper;
import com.xxxx.cms.vo.Perm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PermissionService extends BaseService<Perm,Integer> {

    @Resource
    private PermMapper permissionMapper;
    public List<String> queryUserHasRoleIdsHasModuleIds(Integer userId) {
        return  permissionMapper.queryUserHasRoleIdsHasModuleIds(userId);
    }
}
