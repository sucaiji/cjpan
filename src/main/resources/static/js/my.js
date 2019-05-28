function getInfo(obj) {
    $('#rename-uuid').val(obj.name);
    $('#rename').modal('show');
}

var $renamebtn = $('#rename-btn');
$renamebtn.on('click', function () {
    var uuid = $('#rename-uuid').val();
    var newName = $('#rename-name').val();
    $.ajax({
        url: "/api/rename?uuid=" + uuid + "&name=" + newName,
        type: "GET",
        async: true,        //异步
        processData: false,  //很重要，告诉jquery不要对form进行处理
        contentType: false,  //很重要，指定为false才能形成正确的Content-Type
        success: function (data) {
            alert("重命名成功");
            location.reload(true);

        }, error: function (XMLHttpRequest, errorThrown) {
            alert("服务器出错!");
        }
    });
})


var del = function (uuid) {
    $.ajax({
        url: "/api/delete?uuid=" + uuid,
        type: "GET",
        async: true,        //异步
        processData: false,  //很重要，告诉jquery不要对form进行处理
        contentType: false,  //很重要，指定为false才能形成正确的Content-Type
        success: function (data) {
            alert("成功删除");
            location.reload(true);
        }, error: function (XMLHttpRequest, errorThrown) {
            alert("成功删除");
            location.reload(true);
        }
    });
};

var $exitbtn = $('#exit-btn');
$exitbtn.on('click', function () {
    $.ajax({
        url: "/api/exit",
        type: "GET",
        async: true,        //异步
        processData: false,  //很重要，告诉jquery不要对form进行处理
        contentType: false,  //很重要，指定为false才能形成正确的Content-Type
        success: function (data) {
            alert("成功退出");
            location.reload(true);
        }, error: function (XMLHttpRequest, errorThrown) {
            alert("服务器出错!");
        }
    });
});

$(document).ready(function () {

    // Javascript method's body can be found in assets/js/demos.js
    demo.initDashboardPageCharts();

});

function sidebarActive() {
    var pathname = window.location.pathname;
    switch (pathname) {
        case "/index":
            $("#sidebar-all").addClass("active")
            break;
        case "/":
            $("#sidebar-all").addClass("active")
            break;
        case "/index.html":
            $("#sidebar-all").addClass("active")
            break;
        case "/file/video":
            $("#sidebar-videos").addClass("active")
            break;
        case "/file/image":
            $("#sidebar-gallery").addClass("active")
            break;
        case "/file/document":
            $("#sidebar-documents").addClass("active")
            break;
        case "/file/other":
            $("#sidebar-others").addClass("active")
            break;
        case "/file/music":
            $("#sidebar-musics").addClass("active")
            break;
        default:
            break;
    }
}

sidebarActive();

//开启画廊
$(document).on('click', '[data-toggle="gallery"]', function (event) {
    event.preventDefault();
    $(this).ekkoLightbox({
        alwaysShowClose: true,
    });
});