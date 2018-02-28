function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
}

var parent_uuid = getQueryString("parent_uuid");


var i = -1;
var succeed = 0;
var databgein;  //开始时间
var dataend;    //结束时间
var action = false;    //false检验分片是否上传过(默认); true上传文件


var page = {
    init: function () {
        $("#btn-upload").click(function () {
            databgein = new Date();
            var file = $("#file")[0].files[0];  //文件对象
            isUpload(file);
        });

    }

};

$(function () {
    page.init();
});

function isUpload(file) {

    //构造一个表单，FormData是HTML5新增的

    var form = new FormData();

    var md5;

    var blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice,
        //file = blob;
        chunkSize = 2097152, // read in chunks of 2MB
        chunks = Math.ceil(file.size / chunkSize),
        currentChunk = 0,
        spark = new SparkMD5.ArrayBuffer(),
        frOnload = function (e) {
            //  log.innerHTML+="\nread chunk number "+parseInt(currentChunk+1)+" of "+chunks;
            spark.append(e.target.result); // append array buffer
            currentChunk++;
            if (currentChunk < chunks) {
                loadNext();
            }
            else {
                md5 = spark.end();
                name = file.name;
                form.append("name", name);
                form.append("md5", md5);
                if (parent_uuid != null) {
                    form.append("parent_uuid", parent_uuid);
                }
                //Ajax提交
                $.ajax({
                    url: "api/is_upload",
                    type: "POST",
                    data: form,
                    async: true,        //异步
                    processData: false,  //很重要，告诉jquery不要对form进行处理
                    contentType: false,  //很重要，指定为false才能形成正确的Content-Type
                    success: function (data) {
                        var dataObj = eval(data);
                        if (dataObj.flag == "2") {
                            //没有上传过文件
                            upload(file, md5);
                        } /*else if(dataObj.flag == "2") {
                    //已经上传部分
                    upload(file,md5);

                } */ else if (dataObj.flag == "1") {
                            //文件已经上传过
                            //alert("文件已经上传过,秒传了！！");
                            location.reload(true);
                        }

                    }, error: function (XMLHttpRequest, errorThrown) {

                        alert("已经上传过或者服务器出1错!");

                    }

                });

            }
        },
        frOnerror = function () {
            log.innerHTML += "糟糕，好像哪里错了.";
        };

    function loadNext() {
        var fileReader = new FileReader();
        fileReader.onload = frOnload;
        fileReader.onerror = frOnerror;
        var start = currentChunk * chunkSize,
            end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize;
        fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
    };

    loadNext();


}

/*
 * file 文件对象
 * filemd5 整个文件的md5
*/
function upload(file, filemd5) {

    name = file.name;        //文件名

    size = file.size;        //总大小

    var shardSize = 5 * 1024 * 1024,    //以5MB为一个分片

        shardCount = Math.ceil(size / shardSize);  //总片数


    if (i > shardCount) {

        i = -1;

        i = shardCount;

    } else {

        if (!action) {

            i += 1;  //只有在检测分片时,i才去加1; 上传文件时无需加1

        }

    }

    //计算每一片的起始与结束位置

    var start = i * shardSize,

        end = Math.min(size, start + shardSize);


    //构造一个表单，FormData是HTML5新增的

    var form = new FormData();

    if (!action) {
        form.append("action", "check");  //检测分片是否上传
        $("#param").append("action==check ");

    } else {
        form.append("action", "upload");  //直接上传分片
        form.append("file", file.slice(start, end));  //slice方法用于切出文件的一部分
        $("#param").append("action==upload ");
        if (i + 1 == shardCount) {
            form.append("finish", true);
        } else {
            form.append("finish", false);
        }
    }

    if (parent_uuid != null) {
        form.append("parent_uuid", parent_uuid);
    }

    form.append("filemd5", filemd5);
    form.append("name", name);
    form.append("size", size);
    form.append("total", shardCount);  //总片数
    form.append("index", i + 1);        //当前是第几片

    var ssindex = i + 1;
    $("#param").append("index==" + ssindex + "<br/>");

    //按大小切割文件段　　
    var data = file.slice(start, end);

    var r = new FileReader();
    r.readAsBinaryString(data);

    $(r).on('load', function (e) {
        var bolb = e.target.result;
        var md5 = hex_md5(bolb);
        form.append("md5", md5);


        //Ajax提交

        $.ajax({

            url: "api/upload",
            type: "POST",
            data: form,
            async: true,        //异步
            processData: false,  //很重要，告诉jquery不要对form进行处理
            contentType: false,  //很重要，指定为false才能形成正确的Content-Type
            success: function (data) {
                var dataObj = eval(data);
                var flag = dataObj.flag;

                //服务器返回该分片是否上传过
                if (!action) {
                    if (flag == "4") {
                        //未上传
                        action = true;
                    } else if (flag == "3") {
                        //已上传
                        action = false;
                        ++succeed;
                    }
                    //递归调用                　
                    upload(file, filemd5);
                } else {
                    //服务器返回分片是否上传成功
                    //改变界面
                    ++succeed;
                    $("#output").text(succeed + " / " + shardCount);
                    if (i + 1 == shardCount) {
                        //是否上传完毕
                        dataend = new Date();
                        $("#usetime").append(dataend.getTime() - databgein.getTime());
                        //刷新页面
                        location.reload(true);
                    } else {
                        //已上传成功,然后检测下一个分片
                        action = false;
                        //递归调用                　
                        upload(file, filemd5);
                    }
                }
            }, error: function (XMLHttpRequest, errorThrown) {
                alert("服务器出2错!");
            }
        });
    })
}

