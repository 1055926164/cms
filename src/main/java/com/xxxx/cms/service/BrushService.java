package com.xxxx.cms.service;

import com.xxxx.cms.base.BaseService;
import com.xxxx.cms.dao.BrushMapper;
import com.xxxx.cms.utils.AssertUtil;
import com.xxxx.cms.utils.LoginUserUtil;
import com.xxxx.cms.vo.Brush;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BrushService extends BaseService<Brush,Integer> {

    @Resource
    private BrushMapper brushMapper;

    //查询所有的题目
    public Map<String,Object> queryAllBrush(HttpServletRequest request){
        Map<String,Object> map = new HashMap<>();
        Integer userId = LoginUserUtil.releaseUserIdFromCookie(request);
        //查询
        List<Brush> list = brushMapper.queryAllBrush(userId);
        list.forEach(System.out::println);
        //先设置所有的当前登录用户
        for (int i=0;i<list.size();i++){
            list.get(i).setdId(LoginUserUtil.releaseUserIdFromCookie(request));
        }
        for (int i = 0;i<list.size();i++){
            Integer count = 0;
            Integer num = 0;
            if (list.get(i).getParentId()==-1){
                for (int j =0;j<list.size();j++){
                    if (list.get(j).getParentId()==list.get(i).getBrushId()){
                        count++;
                    }
                    if (list.get(j).getParentId()==list.get(i).getBrushId()&&list.get(j).getAnswerId()!=null&& list.get(j).getUserId().equals(list.get(j).getdId())){

                        num++;
                    }
                }
                list.get(i).setCount(count);
                list.get(i).setNum(num);
            }
            //将标签去除，只保留文本部分
            list.get(i).setQuestion(delHTMLTag(list.get(i).getQuestion()));
        }


        map.put("code",0);
        map.put("msg","");
        map.put("count",list.size());
        map.put("data",list);
        return map;
    }

    public List<Brush> queryByParentId(Integer brushId) {
        //参数校验
        AssertUtil.isTrue(brushId==null,"要查找数据不存在");
        List<Brush> list = brushMapper.queryByParentId(brushId);
        return list;
    }

    public String delHTMLTag(String htmlStr){

        String regEx_script="";

        String regEx_style="";

        String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);

        Matcher m_script=p_script.matcher(htmlStr);

        htmlStr=m_script.replaceAll(""); //过滤script标签

        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);

        Matcher m_style=p_style.matcher(htmlStr);

        htmlStr=m_style.replaceAll(""); //过滤style标签

        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);

        Matcher m_html=p_html.matcher(htmlStr);

        htmlStr=m_html.replaceAll(""); //过滤html标签

        return htmlStr.trim(); //返回文本字符串

    }
}
