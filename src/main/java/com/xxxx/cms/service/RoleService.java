package com.xxxx.cms.service;

import com.github.pagehelper.PageHelper;

import com.github.pagehelper.PageInfo;
import com.xxxx.cms.base.BaseService;
import com.xxxx.cms.dao.ModMapper;
import com.xxxx.cms.dao.PermMapper;
import com.xxxx.cms.dao.RolesMapper;
import com.xxxx.cms.query.RoleQuery;
import com.xxxx.cms.utils.AssertUtil;
import com.xxxx.cms.vo.Perm;
import com.xxxx.cms.vo.Roles;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RoleService extends BaseService<Roles,Integer> {
    @Resource
    private RolesMapper roleMapper;
    @Resource
    private PermMapper permissionMapper;
    @Resource
    private ModMapper moduleMapper;

    public List<Map<String,Object>> queryAllRoles(Integer userId){
        return roleMapper.queryAllRoles(userId);
    }
    public Map<String,Object> queryRolesByParams(RoleQuery roleQuery){
        Map<String,Object> map=new HashMap<String,Object>();
        PageHelper.startPage(roleQuery.getPage(),roleQuery.getLimit());
        PageInfo<Roles> pageInfo=new PageInfo<Roles>(selectByParams(roleQuery));
        map.put("code",0);
        map.put("msg","");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());
        return  map;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public  void saveRole(Roles role){
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"请输入角色名称!");
        AssertUtil.isTrue(null !=roleMapper.selectByRoleName(role.getRoleName()),"该角色已存在!");
        role.setIsValid(1);
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());
        AssertUtil.isTrue(insertSelective(role)<1,"角色记录添加失败!");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public  void updateRole(Roles role){
        AssertUtil.isTrue(role.getId()==null,"更新记录不存在!");
        Roles temp =roleMapper.selectByPrimaryKey(role.getId());
        AssertUtil.isTrue(null ==temp ,"更新记录不存在!");
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"请输入用户名");
        temp=roleMapper.selectByRoleName(role.getRoleName());
        AssertUtil.isTrue(temp!=null&&(!temp.getId().equals(role.getId())),"角色名已存在不可重复使用!");
        role.setUpdateTime(new Date());
        AssertUtil.isTrue(roleMapper.updateByPrimaryKeySelective(role)<1,"修改失败");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteRole(Integer roleId){
        AssertUtil.isTrue(null == roleId,"待删除的记录不存在!");
        Roles role=selectByPrimaryKey(roleId);
        AssertUtil.isTrue(null == role,"待删除的记录不存在!");

        role.setIsValid(0);
        role.setUpdateTime(new Date());
        AssertUtil.isTrue(roleMapper.updateByPrimaryKeySelective(role)<1,"角色记录删除失败!");
    }

    public void addGrant(Integer[] mids, Integer roleId) {
        /**
         *   授权思路  核心表 t_permission
         *      直接批量添加  不适合 有可能是对角色权限进行更新(权限更新后有可能添加新的菜单 删除原始菜单  甚至情况权限)
         *  推荐: 角色存在原始权限时  先删除原始权限记录  然后批量添加新的角色权限
         */
        int total = permissionMapper.countPermissionByRoleId(roleId);
        if(total>0){
            AssertUtil.isTrue(permissionMapper.deletePermissionByRoleId(roleId)!=total,"角色授权失败!");
        }

        if(null !=mids && mids.length>0){
            List<Perm> permissions=new ArrayList<Perm>();
            for(Integer mid:mids){
                Perm permission=new Perm();
                permission.setCreateDate(new Date());
                permission.setModuleId(mid);
                permission.setRoleId(roleId);
                permission.setUpdateDate(new Date());
                permission.setAclValue(moduleMapper.selectByPrimaryKey(mid).getOptValue());
                permissions.add(permission);
            }
            AssertUtil.isTrue(permissionMapper.insertBatch(permissions)!=permissions.size(),"角色授权失败!");
        }
    }
}
