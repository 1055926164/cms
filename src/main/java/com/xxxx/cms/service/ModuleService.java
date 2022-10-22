package com.xxxx.cms.service;

import com.xxxx.cms.base.BaseService;
import com.xxxx.cms.dao.ModMapper;
import com.xxxx.cms.dao.PermMapper;
import com.xxxx.cms.model.TreeDto;
import com.xxxx.cms.vo.Mod;
import com.xxxx.cms.vo.Perm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModuleService extends BaseService<Mod,Integer> {

    @Resource
    private ModMapper moduleMapper;
    @Resource
    private PermMapper permMapper;

    public List<TreeDto> queryAllModules(Integer roleId){
        List<TreeDto> treeDtos = moduleMapper.queryAllModules();
        // 查询角色已分配的菜单id
        List<Integer>  mids =permMapper.queryRoleHasAllMids(roleId);
        if(null !=mids && mids.size()>0){
            treeDtos.forEach(t->{
                if(mids.contains(t.getId())){
                    // 角色已分配该菜单
                    t.setChecked(true);
                }
            });
        }
        return treeDtos;
    }
}
