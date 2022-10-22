package com.xxxx.cms.dao;

import com.xxxx.cms.base.BaseMapper;
import com.xxxx.cms.model.TreeDto;
import com.xxxx.cms.vo.Mod;

import java.util.List;

public interface ModMapper extends BaseMapper<Mod,Integer> {
    public List<TreeDto>  queryAllModules();
}