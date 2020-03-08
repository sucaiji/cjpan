function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
}

var parentUuid = getQueryString("parentUuid");


var databgein;  //开始时间
var dataend;    //结束时间


var page = {
    init: function () {
        $("#btn-upload").click(function () {
            databgein = new Date();
            var file = $("#file")[0].files[0];  //文件对象
            $("#param").append("开始上传<br/>");
            $("#upload-progress").css("visibility", "visible");
            $("#upload-progress-bar").attr("aria-valuenow", "100");
            $("#upload-progress-bar").css("width", "100%");
            $("#upload-progress-text").text("正在生成md5值");
            upload(file);
        });

    }

};

$(function () {
    page.init();
});

function upload(file) {
    //调用接口获取uuid
    var uuid = null;
    $.ajax({
        url: "api/getUuid",
        type: "GET",
        async: false,        //异步
        processData: false,  //很重要，告诉jquery不要对form进行处理
        contentType: false,  //很重要，指定为false才能形成正确的Content-Type
        success: function (data) {
            uuid = eval(data);
        }
    });



    uploadRecursion(file, uuid, 0);

}

/*
 * 分片上传文件的递归方法
 * file 文件对象
 * filemd5 整个文件的md5
*/
function uploadRecursion(file, uuid, index) {
    var name = file.name;        //文件名
    size = file.size;        //总大小
    var shardSize = 5 * 1024 * 1024;    //以5MB为一个分片
    var shardCount = Math.ceil(size / shardSize);  //总片数

    //计算每一片的起始与结束位置
    var start = index * shardSize;
    var end = Math.min(size, start + shardSize);


    //构造一个表单，FormData是HTML5新增的
    var form = new FormData();

    form.append("file", file.slice(start, end));  //slice方法用于切出文件的一部分
    if (index + 1 == shardCount) {
        form.append("finish", true);
    } else {
        form.append("finish", false);
    }


    if (parentUuid != null) {
        form.append("parentUuid", parentUuid);
    }

    form.append("uuid", uuid);
    form.append("name", name);
    form.append("size", size);
    form.append("total", shardCount);  //总片数
    form.append("index", index + 1);        //当前是第几片

    // $("#param").append("index==" + index + "<br/>");
    var percent = GetPercent(index + 1, shardCount);
    $("#upload-progress-bar").css("width", percent + "%");
    $("#upload-progress-bar").attr("aria-valuenow", percent);
    $("#upload-progress-text").text(percent + "%");

    //按大小切割文件段　　
    var data = file.slice(start, end);

    var r = new FileReader();
    r.readAsBinaryString(data);


    $(r).on('load', function (e) {
        var bolb = e.target.result;
        $.ajax({
            url: "api/upload",
            type: "POST",
            data: form,
            async: true,        //异步
            processData: false,  //很重要，告诉jquery不要对form进行处理
            contentType: false,  //很重要，指定为false才能形成正确的Content-Type
            success: function (data) {
                //服务器返回分片是否上传成功
                //改变界面
                ++index;
                if (index == shardCount) {
                    //是否上传完毕
                    dataend = new Date();
                    $("#usetime").append(dataend.getTime() - databgein.getTime());

                    checkSuccess(uuid);
                } else {
                    //递归调用                　
                    uploadRecursion(file, uuid, index);
                }
            }, error: function (XMLHttpRequest, errorThrown) {
                uploadRecursion(file, uuid, index);
            }
        });
    })
}


function GetPercent(num, total) {
    num1 = parseFloat(num);
    total1 = parseFloat(total);
    if (isNaN(num1) || isNaN(total1)) {
        return "-";
    }
    return total1 <= 0 ? "0" : (Math.round(num1 / total1 * 10000) / 100.00);
}


function checkSuccess(uuid) {
    setInterval(function (uuid) {
        var form = new FormData();
        form.append("uuid", uuid);
        $.ajax({
            url: "api/checkUpload",
            type: "POST",
            data: form,
            async: true,
            processData: false,
            contentType: false,
            success: function (data) {
                var dataObj = eval(data);
                var success = dataObj.success;
                // dataend = new Date();
                // $("#usetime").append(dataend.getTime() - databgein.getTime());
                if (success) {
                    alert("上传成功！");
                    //刷新页面
                    location.reload(true);
                } else {
                    $("#upload-progress-text").text("正在校验md5值");
                    // $("#param").append("正在检查md5<br/>");
                    //递归调用
                }
            }, error: function (XMLHttpRequest, errorThrown) {
                alert("checkMd5出错！");
            }
        });
    }, 2000)




}

