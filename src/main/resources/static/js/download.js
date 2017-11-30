function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
}
var uuid=getQueryString("uuid");
var url="api/visit";
if(uuid!=null){
    url+="?uuid="+uuid;
}
$.ajax({
    url: url,
    type: "POST",
    async: true,        //异步
    processData: false,  //很重要，告诉jquery不要对form进行处理
    contentType: false,  //很重要，指定为false才能形成正确的Content-Type
    success: function(data){
        var content="";
        var dataObj=eval(data);
        for (var i = 0; i<dataObj.length; i++) {
            content+="<li><a href=";
            if(dataObj[i].wasDir){
                content+="'?uuid="+dataObj[i].uuid+"'";
            }else {
                content+="'download?uuid="+dataObj[i].uuid+"'";
            }
            content+="><img class='am-img-thumbnail am-img-bdrs' src=";
            if(dataObj[i].wasDir){
                content+="'img/tubiao/folder.png'";
            }else {
                content+="'img/tubiao/documents.png'";
            }
            content+="alt=''/>" +
                "           <div class='gallery-title'>"+dataObj[i].name+"</div>" +
                "           <div class='gallery-desc'>2375-09-26</div>" +
                "    </a>" +
                " </li>"



        }
        document.getElementById("file-list").innerHTML=content;


    },error: function(XMLHttpRequest, errorThrown) {

        alert("服务器出1错!");

    }

});