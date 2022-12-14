layui.use(['table','laydate','layer','form'],function(){
    var laydate = layui.laydate;
    var layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        table = layui.table;
    var form = layui.form;

    //时间选择器
    laydate.render({
        elem: '#date'
        ,type: 'date'
        ,max: 7 //7天后
        ,trigger: 'click'
    });

    //搜索按钮监听
    form.on("submit(search)", function (data) {

        window.location.replace(ctx+"/conferenceQuestions/index?createTime="+$("[name='createTime']").val()
        +"&roomNumber="+$("[name='roomNumber']").val()+"&rankId="+$("[name='rankId']").val());

        return false;
    });

    //提交按钮监听--进入会议
    form.on("submit(contents)", function (obj) {
        var title="<h2>会议-会议ing</h2>";
            //访问成功
            layui.layer.open({
                title : title,
                type : 2,
                area:["900px","550px"],
                //最大最小化
                maxmin:true,
                content : ctx+"/conferenceQuestions/toQuestionsPage?id="+obj.field.id
        });
        return false;
    });


    //提交按钮监听-问题汇总
    form.on("submit(allQuestions)", function () {

            layui.layer.open({
                title : "<h2>会议-问题汇总</h2>",
                type : 2,
                area:["900px","550px"],
                //最大最小化
                maxmin:true,
                content : ctx+"/conferenceQuestions/toAllQuestionsPage"
            });

        return false;
    });

});
