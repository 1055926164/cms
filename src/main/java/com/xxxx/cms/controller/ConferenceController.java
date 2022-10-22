package com.xxxx.cms.controller;


import com.xxxx.cms.annotaions.RequirePermission;
import com.xxxx.cms.base.BaseController;
import com.xxxx.cms.base.ResultInfo;
import com.xxxx.cms.query.ConferenceNowQuery;
import com.xxxx.cms.query.ConferenceQuery;
import com.xxxx.cms.service.ConferenceService;
import com.xxxx.cms.vo.Conference;
import com.xxxx.cms.vo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/conference")
public class ConferenceController extends BaseController {
    @Resource
    private ConferenceService conferenceService;

    /**
     * 直接跳转会议详情页面
     * @return
     */

    @RequestMapping("index")
    public String index(){
        return "conference/Conference";
    }

    //查询 所有会议记录

    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> queryConferenceByParams(ConferenceQuery conferenceQuery){
        System.out.println(conferenceQuery);
        return conferenceService.queryConferenceByParams(conferenceQuery);
    }


    /**
     * 进入添加修改页面
     */

    @RequestMapping("toAddAndUpdataPage")
    public String toAddAndUpdataPage(Integer id, HttpServletRequest request){
        if (id!=null){
            Conference conference = conferenceService.selectByPrimaryKey(id);
            request.setAttribute("conference",conference);
        }
        return "conference/add_updata";
    }


    /**
     * 查询所有的等级
     * @return
     */

    @PostMapping("queryAllRank")
    @ResponseBody
    public List<Object> queryAllRank(){
        return conferenceService.queryAllRank();
    }

    /**
     * 查询所有的房间
     * @return
     */

    @PostMapping("queryAllRoom")
    @ResponseBody
    public List<Object> queryAllRoom(){
        return conferenceService.queryAllRoom();
    }




    /**
     * 添加或修改会议
     *      添加会议时将用户名设置
     * @param conference
     * @param session
     * @return
     */

    @PostMapping("toAddOrUpdate")
    @ResponseBody
    public ResultInfo toAddOrUpdate(Conference conference, HttpSession session){

        if (conference.getId()==null){
            //表示添加会议
            User user = (User) session.getAttribute("user");
            conference.setUserName(user.getTrueName());
            conferenceService.toAddOrUpdate(conference);
            return success("会议添加成功!");
        }else {
            conferenceService.toAddOrUpdate(conference);
            return success("会议修改成功!");
        }

    }

    /**
     * 会议室删除
     * @param ids
     * @return
     */

    @PostMapping("delete")
    @ResponseBody
    public ResultInfo delete(Integer[] ids){
        System.out.println(ids);
        conferenceService.deleteConference(ids);
        return success("会议删除成功!");
    }


    /**
     * 进入会议详情界面
     */

    @RequestMapping("toDetailsConferencePage")
    public String toDetailsConferencePage(Integer id, HttpServletRequest request){
        String details=conferenceService.queryDetailsById(id);
        request.setAttribute("id",id);
        request.setAttribute("details",details);
        return "conference/details";
    }

    /**
     * 加载数据 查会议人员 根据会议级别
     *      根据组别查会议人员
     *      查会议详情信息
     * @param id
     * @param request
     * @return
     */

    @RequestMapping("queryParticipants")
    @ResponseBody
    public List<Map<String,Object>> queryParticipantsAndDetails(Integer id, HttpServletRequest request){

        //根据组别查会议人员
        return conferenceService.queryParticipantsById(id);
    }

}
