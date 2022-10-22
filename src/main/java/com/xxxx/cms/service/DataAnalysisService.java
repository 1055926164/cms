package com.xxxx.cms.service;

import com.xxxx.cms.base.BaseService;
import com.xxxx.cms.dao.DataAnalysisServiceMapper;
import com.xxxx.cms.query.ConfQuery;
import com.xxxx.cms.query.DataAnalysisQuery;
import com.xxxx.cms.query.DataAnalysisResult;
import com.xxxx.cms.vo.Speed;
import com.xxxx.cms.vo.Speed2;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DataAnalysisService extends BaseService<Speed2,Integer> {
    @Resource
    private DataAnalysisServiceMapper dataAnalysisServiceMapper;

    DecimalFormat df = new DecimalFormat("0.00");
    /*查询所有的学习进度返回一个list集合*/
    public DataAnalysisResult<List> selectSpeed(Integer groupId, String updateTime) {
        Date date = new Date();
        DataAnalysisResult<List> dataAnalysisResult = new DataAnalysisResult<>();
        if (groupId == null && dataAnalysisServiceMapper.selectGroupId() >= 1) {
            groupId = 1;
        }

        if (updateTime == null || updateTime == "") {
            Date[] date1 = dataAnalysisServiceMapper.selectUpdateTime();
            date = date1[date1.length - 1];
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");//2018-08-13 10:22:48.883
            try {
                date = format.parse(updateTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (groupId==0){
            //查询班级有关数据
            List list=selectAllAverage(date);
            dataAnalysisResult.setResult(list);
            dataAnalysisResult.setGroupId(0);
            return  classAnalysis(dataAnalysisResult, date);
        }else {
            //查询所有的学习进度和返回一个list集合
            List list = selectSpeedByGroupId(groupId, date);
            //智能分析判断进度情况
            dataAnalysisResult.setResult(list);
            dataAnalysisResult.setGroupId(groupId);
            return analysis(groupId, dataAnalysisResult, date);
        }
    }

    private DataAnalysisResult<List> classAnalysis(DataAnalysisResult<List> dataAnalysisResult, Date date) {
        int groups= dataAnalysisServiceMapper.selectGroupId();

        double avg=0.0;//
        double avg2=0.0;
        double[] vedioAvg=new double[groups];
        double[] codeAvg=new double[groups];
        double[] noteAvg=new double[groups];
        double[]  signAvg=new double[groups];
        int listSize=0;
        for (int i = 0; i <groups; i++) {
            List<DataAnalysisQuery> list= dataAnalysisServiceMapper.selectSpeed(i+1,date);
            if (list.size()<1) {
                listSize+=1;
            }
            if (listSize==groups){
                dataAnalysisResult.setSpeedVedioAverage("暂无数据");
                //求代码进度平均值
                dataAnalysisResult.setSpeedCodeAverage("暂无数据");
                dataAnalysisResult.setSpeedVedioMin("暂无数据");
                dataAnalysisResult.setSpeedCodeMin("暂无数据");
                dataAnalysisResult.setNoteMin("暂无数据");
                List<ConfQuery> conf = dataAnalysisServiceMapper.selectConfClass(date);
                if (conf.size() == 0) {
                    dataAnalysisResult.setQuestion("班级今日暂无问题");
                    dataAnalysisResult.setQuestionState(-1);
                } else {
                    if (dataAnalysisResult.getQuestion() == null) {
                        dataAnalysisResult.setQuestion("");
                    }
                    String str3 = "";
                    for (ConfQuery confQuery : conf) {
                        if (confQuery.getQuestionState() == 0) {
                            str3 = "未知";
                        } else if (confQuery.getQuestionState() == 1) {
                            str3 = "已解决";
                        } else {
                            str3 = "未解决";
                        }
                        dataAnalysisResult.setQuestion(dataAnalysisResult.getQuestion() + "\n" + confQuery.getQuestion() + "," + str3 + ";");
                    }
                }
                dataAnalysisResult.setSignMin("暂无数据");
                return dataAnalysisResult;
            }
                double count = qiuAverage(list)[0];
                double count1 = qiuAverage(list)[1];
                //求视频进度平均值和
                avg += count;
                //得到班级平均进度数组
                vedioAvg[i] = count;
                //求代码进度平均值
                avg2 += count1;
                //得到班级平均进度数组
                codeAvg[i] = count1;
                //得到班级平均笔记情况数组
                noteAvg[i] = qiuAverage(list)[2];
                //得到班级平均签到情况数组
                signAvg[i] = qiuAverage(list)[3];
        }
        String str = "";
        for (int i = 0; i < noteAvg.length; i++) {
            if (noteAvg[i] <=0.9) {
                str +=i+1+ "组,";
            }
        }
        String str2 = "";
        for (int i = 0; i < signAvg.length; i++) {
            if (signAvg[i] <=0.9) {
                str2 +=i+1+ "组,";
            }
        }
        Arrays.sort(noteAvg);
        Arrays.sort(signAvg);
        double vedioSpeedEndMin = Arrays.stream(vedioAvg).min().getAsDouble();
        double codeSpeedEndMin = Arrays.stream(codeAvg).min().getAsDouble();
        double noteMin = noteAvg[0];
        double signMin = signAvg[0];
        dataAnalysisResult.setSpeedVedioAverage( "班级视频平均进度为" +df.format( avg/groups) + "个视频");
        dataAnalysisResult.setSpeedCodeAverage( "班级码平均进度为" + df.format(avg2/groups)+ "个部分");
        String strV="";
        if (avg/groups - vedioSpeedEndMin > 2.0||vedioSpeedEndMin==0.0) {
            int index = 0;
            //视频进度慢者
            for (int i = 0; i < groups; i++) {
                if (vedioAvg[i] == vedioSpeedEndMin) {
                    index = i;
                    strV+=index+1+"组"+",";
                }
            }
            strV = strV.substring(0, strV.lastIndexOf(','));
            dataAnalysisResult.setSpeedVedioMin(strV+"学习进度落后班内视频平均进度超过2个平均点,需要加油");
        } else {
            dataAnalysisResult.setSpeedVedioMin("班级视屏进度整体进度良好,请继续保持");
        }
        String strC="";
        if (avg/groups - codeSpeedEndMin < 2.0||codeSpeedEndMin==0.0) {
            int index = 0;
            //代码进度慢者
            for (int i = 0; i <groups; i++) {
                if (codeAvg[i] == codeSpeedEndMin) {
                    index = i;
                    strC+=index+1+"组"+",";
                }
            }
            strC= strC.substring(0, strC.lastIndexOf(','));
            dataAnalysisResult.setSpeedCodeMin(strC+"代码进度落后班内超过2个平均点,需要加强代码的练习");
        } else {
            dataAnalysisResult.setSpeedCodeMin("班级代码进度整体进度良好,请继续保持");
        }
        if (noteMin<0.9){
            str = str.substring(0, str.lastIndexOf(','));
            dataAnalysisResult.setNoteMin(str + "需要监督组员的笔记完成情况");
        } else {
            dataAnalysisResult.setNoteMin("班级笔记完成率大于90%,请继续保持");
        }
        if (signMin<0.9){
            str2 = str2.substring(0, str2.lastIndexOf(','));
            dataAnalysisResult.setSignMin(str2 + "需要监督组内纪律,避免迟到现象发生");
        } else {
            dataAnalysisResult.setSignMin("班级签到率超过90%,请继续保持");
        }
        //班级问题分析汇总

        List<ConfQuery> conf = dataAnalysisServiceMapper.selectConfClass(date);
        if (conf.size() == 0) {
            dataAnalysisResult.setQuestion("班级今日暂无问题");
            dataAnalysisResult.setQuestionState(-1);
        } else {
            if (dataAnalysisResult.getQuestion() == null) {
                dataAnalysisResult.setQuestion("");
            }
            String str3 = "";
            for (ConfQuery confQuery : conf) {
                if (confQuery.getQuestionState() == 0) {
                    str3 = "未知";
                } else if (confQuery.getQuestionState() == 1) {
                    str3 = "已解决";
                } else {
                    str3 = "未解决";
                }
                dataAnalysisResult.setQuestion(dataAnalysisResult.getQuestion() + "\n" + confQuery.getQuestion() + "," + str3 + ";");
            }
        }
        return dataAnalysisResult;
    }

    //查询一共有多少个组
    public int selectGroupId() {
        return dataAnalysisServiceMapper.selectGroupId();}
    //查询进度日期集合
    public String[] selectDate() {
        Date[] dates = dataAnalysisServiceMapper.selectUpdateTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String[] date = new String[dates.length];
        for (int i = 0; i < dates.length; i++) {
            String strDate = dateFormat.format(dates[i]);
            date[i] = strDate;
        }
        return date;
    }

    private DataAnalysisResult<List> analysis(Integer groupId, DataAnalysisResult<List> dataAnalysisResult, Date updateTime) {
        List<DataAnalysisQuery> list1 = dataAnalysisServiceMapper.selectSpeed(groupId, updateTime);
        String[] name=dataAnalysisServiceMapper.selectNameByGroupId(groupId);
        List<String[]> list=selectSpeedByGroupId(groupId,updateTime);
        //if (list1.size()!=0) {
            int[] vedioSpeedEnd=new int[name.length];
            int[] codeSpeedEnd=new int[name.length];
            int[] note=new int[name.length];
            int[] sign=new int[name.length];
            for (int i = 0; i < list.get(1).length; i++) {
                vedioSpeedEnd[i] = Integer.parseInt(list.get(1)[i]);
                codeSpeedEnd[i] = Integer.parseInt(list.get(2)[i]);
                note[i] = Integer.parseInt(list.get(3)[i]);
                sign[i] = Integer.parseInt(list.get(4)[i]);
            }

      /*  if (list1.size()!=0) {
            int[] vedioSpeedEnd = new int[name.length];
            int[] codeSpeedEnd = new int[name.length];
            int[] note = new int[name.length];
            int[] sign = new int[name.length];
            for (int i = 0; i <name.length; i++) {
                vedioSpeedEnd[i] = Integer.parseInt(list1.get(i).getVideoSpEnd());
                codeSpeedEnd[i] = Integer.parseInt(list1.get(i).getCodeSpEnd());
                note[i] = Integer.parseInt(list1.get(i).getNote());
                sign[i] = Integer.parseInt(list1.get(i).getSign());
            }*/

            int vedioSpeedEndMin = 0;
            int vedioSpeedEndMax= 0;
            int codeSpeedEndMin = 0;
            int codeSpeedEndMax= 0;
            OptionalInt optionalInt = Arrays.stream(vedioSpeedEnd).min();
            OptionalInt optionalInt1 = Arrays.stream(codeSpeedEnd).min();
            OptionalInt optionalInt2 = Arrays.stream(vedioSpeedEnd).max();
            OptionalInt optionalInt3 = Arrays.stream(codeSpeedEnd).max();
            if (optionalInt2.isPresent()) { vedioSpeedEndMax = optionalInt.getAsInt(); }
            if (optionalInt.isPresent()) { vedioSpeedEndMin = optionalInt.getAsInt(); }
            if (optionalInt1.isPresent()) { codeSpeedEndMin = optionalInt.getAsInt(); }
            if (optionalInt3.isPresent()) { codeSpeedEndMax = optionalInt.getAsInt(); }

            String noteMinStr = "";
            for (int i = 0; i < note.length; i++) {
                if (note[i] == 0) {
                    noteMinStr += name[i] + ",";
                }
            }
            String signMinstr = "";
            for (int i = 0; i < sign.length; i++) {
                if (sign[i] == 0) {
                    signMinstr += name[i] + ",";

                }
            }

            Arrays.sort(note);
            Arrays.sort(sign);
            int noteMin = note[0];
            int signMin = sign[0];

            double[] a = qiuAverage(list1);
            //求视频进度平均值
            if (a[0]!=0){dataAnalysisResult.setSpeedVedioAverage(groupId+"组视频平均进度为" + df.format(a[0]) + "个视频");}
            else {dataAnalysisResult.setSpeedVedioAverage(groupId+"组视频平均进度为" + df.format(a[0]) + "个视频,该组可能没有进行数据汇总");}

            //求代码进度平均值
            if (a[1]!=0){ dataAnalysisResult.setSpeedCodeAverage(groupId+ "组代码平均进度为" +df.format(a[1])+ "个部分");}
            else { dataAnalysisResult.setSpeedCodeAverage(groupId+ "组代码平均进度为" +df.format(a[1])+ "个部分,该组可能没有进行数据汇总");}


                //进度慢的评价
            String strV="";
            String strV2="";
            if (a[0] - vedioSpeedEndMin > 3) {
                int index;
                //视频进度慢者
                for (int i = 0; i < vedioSpeedEnd.length; i++) {
                    if (vedioSpeedEnd[i] <=a[0]-3) {
                        index = i;
                        strV+=name[index]+",";
                    }
                }
                strV = strV.substring(0, strV.lastIndexOf(','));
                strV+="同学进度落后组内视频平均进度超过3个平均点,需要加油";

            } else { if (vedioSpeedEndMax!=0){
                    strV+=groupId+ "组视屏进度整体进度良好,请继续保持";
                } }
            if (vedioSpeedEndMin==0) {
                int index;
                //视频进度慢者
                for (int i = 0; i < vedioSpeedEnd.length; i++) {
                    if (vedioSpeedEnd[i] == 0) {
                        index = i;
                        strV2+=name[index]+",";
                    }
                }
                strV2 = strV2.substring(0, strV2.lastIndexOf(','));
                if (strV.length()<1){
                    strV+=strV2+ "同学可能没有填写进度";
                }else {
                    strV+="--------->其中"+strV2+ "同学可能没有填写进度";
                }

            }
            dataAnalysisResult.setSpeedVedioMin(strV);

            String strC="";
            String strC2="";
            if (a[0] - vedioSpeedEndMin > 3) {
                int index;
                //视频进度慢者
                for (int i = 0; i < codeSpeedEnd.length; i++) {
                    if (codeSpeedEnd[i] == codeSpeedEndMin) {
                        index = i;
                        strC+=name[index]+",";
                    }
                }

                strC = strC.substring(0, strC.lastIndexOf(','));
                strC+= "同学代码进度落后组内超过3个平均点,需要加强代码的练习";

            } else { if (codeSpeedEndMax!=0){
                strV+=groupId+ "组代码进度整体进度良好,请继续保持";
            } }
            if (codeSpeedEndMin==0) {
                int index;
                //视频进度慢者
                for (int i = 0; i < codeSpeedEnd.length; i++) {
                    if (codeSpeedEnd[i] == 0) {
                        index = i;
                        strC2+=name[index]+",";
                    }
                }
                strC2 = strC2.substring(0, strC2.lastIndexOf(','));
                if (strC.length()<1){
                    strC+=strC2+ "同学可能没有填写进度";
                }else {
                    strC+="--------->其中"+strC2+ "同学可能没有填写进度";
                }

            }
            dataAnalysisResult.setSpeedCodeMin(strC);

           /* String strC="";
            String strC2="";
            if (a[1] - codeSpeedEndMin > 3||codeSpeedEndMin==0) {
                int index = 0;
                //代码进度慢者
                for (int i = 0; i < codeSpeedEnd.length; i++) {
                    if (codeSpeedEnd[i] == codeSpeedEndMin) {
                        index = i;
                        strC+=name[index]+",";
                    }
                }
                strC = strC.substring(0, strC.lastIndexOf(','));
                dataAnalysisResult.setSpeedCodeMin(strC+ "同学代码进度落后组内超过3个平均点,需要加强代码的练习");

            } else {
                dataAnalysisResult.setSpeedCodeMin(groupId + "组代码进度整体进度良好,请继续保持");
            }*/
            //笔记情况分析评价
            if (noteMin == 0) {
                noteMinStr = noteMinStr.substring(0, noteMinStr.lastIndexOf(','));
                dataAnalysisResult.setNoteMin(noteMinStr + "未完成笔记,需要加强知识点的梳理");
            } else {
                dataAnalysisResult.setNoteMin("笔记统一都已完成,请继续保持");
            }
            //签到情况分析
            if (signMin == 0) {
                signMinstr = signMinstr.substring(0, signMinstr.lastIndexOf(','));
                dataAnalysisResult.setSignMin(signMinstr + "未签到,需要加强组内纪律,避免发生迟到现象");
            } else {
                dataAnalysisResult.setSignMin("都已完成签到,请继续保持");
            }
            //综合评价
            //小组问题分析汇总
            List<ConfQuery> conf = dataAnalysisServiceMapper.selectConf(groupId, updateTime);
            if (conf.size() == 0) {
                dataAnalysisResult.setQuestion("小组今日暂无问题");
                dataAnalysisResult.setQuestionState(-1);
            } else {
                if (dataAnalysisResult.getQuestion() == null) {
                    dataAnalysisResult.setQuestion("");
                }
                String str = "";
                for (ConfQuery confQuery : conf) {
                    if (confQuery.getQuestionState() == 0) {
                        str = "未知";
                    } else if (confQuery.getQuestionState() == 1) {
                        str = "已解决";
                    } else {
                        str = "未解决";
                    }
                    dataAnalysisResult.setQuestion(dataAnalysisResult.getQuestion() + "\n" + confQuery.getQuestion() + "," + str + ";");
                }
            }
        /*}else {
            dataAnalysisResult.setSpeedVedioAverage("暂无数据");
            //求代码进度平均值
            dataAnalysisResult.setSpeedCodeAverage("暂无数据");
            dataAnalysisResult.setSpeedVedioMin("暂无数据");
            dataAnalysisResult.setSpeedCodeMin("暂无数据");
            dataAnalysisResult.setNoteMin("暂无数据");
            dataAnalysisResult.setQuestion("暂无数据");
            dataAnalysisResult.setSignMin("暂无数据");
        }*/
        return dataAnalysisResult;
    }
    private List<String[]> selectSpeedByGroupId(Integer groupId, Date updateTime) {
        String[] name =  dataAnalysisServiceMapper.selectNameByGroupId(groupId);//new String[list.size()];
        List<DataAnalysisQuery> list = dataAnalysisServiceMapper.selectSpeed(groupId, updateTime);

        List list1 = new ArrayList<>();

        String[] vedioSpeedEnd = new String[name.length];
        String[] codeSpeedEnd = new String[name.length];
        String[] note = new String[name.length];
        String[] sign = new String[name.length];
        for (int i = 0; i <name.length; i++) {
            vedioSpeedEnd[i] = String.valueOf(0);
            codeSpeedEnd[i] = String.valueOf(0);
            note[i] = String.valueOf(0);
            sign[i] = String.valueOf(0);
        }
        for (int j = 0; j < list.size(); j++) {
            for (int i = 0; i < name.length; i++) {
                if (name[i].equals(list.get(j).getTrueName())){
                    vedioSpeedEnd[i] = list.get(j).getVideoSpEnd();
                    codeSpeedEnd[i] = list.get(j).getCodeSpEnd();
                    note[i] = list.get(j).getNote();
                    sign[i] = list.get(j).getSign();
                }
            }
        }
        list1.add(name);
        list1.add(vedioSpeedEnd);
        list1.add(codeSpeedEnd);
        list1.add(note);
        list1.add(sign);
        System.out.println(Arrays.toString(name));
        return list1;
    }
    //根据组号和时间查询当天的每组平均视屏,代码,笔记,签到进度和情况
    private List selectAllAverage(Date updateTime) {
        List list = new ArrayList<>();
        int x= dataAnalysisServiceMapper.selectGroupId();
        //得到存放数据的数组
        String[] name = new String[x];
        String[] vedioSpeedEnd = new String[x];
        String[] codeSpeedEnd = new String[x];
        String[] note = new String[x];
        String[] sign = new String[x];
        //循环得到数据
        for (int j = 1; j <=x ; j++) {
            name[j-1]=j+"组";
            List<DataAnalysisQuery> speed = dataAnalysisServiceMapper.selectSpeed(j, updateTime);
            vedioSpeedEnd[j-1]= String.valueOf(qiuAverage(speed)[0]);
            codeSpeedEnd[j-1]= String.valueOf(qiuAverage(speed)[1]);
            note[j-1]= String.valueOf(qiuAverage(speed)[2]);
            sign[j-1]= String.valueOf(qiuAverage(speed)[3]);
        }
        list.add(name);
        list.add(vedioSpeedEnd);
        list.add(codeSpeedEnd);
        list.add(note);
        list.add(sign);
        return list;
    }
    //求单组各个进度的平均值
    private double[] qiuAverage( List<DataAnalysisQuery> speed){
        //循环得到小组平均视屏进度
        double[] allAverage=new double[4];
        int[] vedioSpeedEnd2 = new int[speed.size()];
        int[] codeSpeedEnd2 = new int[speed.size()];
        double[] note2 = new double[speed.size()];
        double[] sign2 = new double[speed.size()];
        for (int i = 0; i < speed.size(); i++) {
            vedioSpeedEnd2[i] = Integer.parseInt(speed.get(i).getVideoSpEnd());
            codeSpeedEnd2[i] = Integer.parseInt(speed.get(i).getCodeSpEnd());
            note2[i] = Integer.parseInt(speed.get(i).getNote());
            sign2[i] = Integer.parseInt(speed.get(i).getSign());
        }
        //求视频进度平均值
        double average = 0;
        for (int i = 0; i < vedioSpeedEnd2.length; i++) {
            average += vedioSpeedEnd2[i];
            if (i == vedioSpeedEnd2.length - 1) {
                average /= i + 1;
            }
        }
        allAverage[0]=Double.parseDouble(df.format(average));
        double average2 = 0;
        //求代码进度平均值
        for (int i = 0; i < codeSpeedEnd2.length; i++) {
            average2 += codeSpeedEnd2[i];
            if (i == codeSpeedEnd2.length - 1) {
                average2 /= i + 1;
            }
        }
        allAverage[1]=Double.parseDouble(df.format(average2));
        //求笔记平均值
        double average3 = 0;
        for (int i = 0; i <  note2.length; i++) {
            average3 +=  note2[i];
            if (i ==  note2.length - 1) {
                average3 /= i + 1;
            }
        }
        allAverage[2]= Double.parseDouble(df.format(average3));
        //求签到情况平均值
        double average4 = 0;
        for (int i = 0; i <  sign2.length; i++) {
            average4 +=  sign2[i];
            if (i ==  sign2.length - 1) {
                average4 /= i + 1;
            }
        }
        allAverage[3]=average4;
        return allAverage;
    }

}
