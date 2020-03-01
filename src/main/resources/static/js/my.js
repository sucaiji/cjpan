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


function del(uuid) {


    Swal.fire({
        type: 'warning', // 弹框类型
        title: '删除', //标题
        text: "删除后将无法恢复，请谨慎操作！", //显示内容

        confirmButtonColor: '#3085d6',// 确定按钮的 颜色
        confirmButtonText: '确定',// 确定按钮的 文字
        showCancelButton: true, // 是否显示取消按钮
        cancelButtonColor: '#d33', // 取消按钮的 颜色
        cancelButtonText: "取消", // 取消按钮的 文字

        focusCancel: true, // 是否聚焦 取消按钮
        reverseButtons: true  // 是否 反转 两个按钮的位置 默认是  左边 确定  右边 取消
    }).then((isConfirm) => {
        //判断 是否 点击的 确定按钮
        if (isConfirm.value) {
            $.ajax({
                url: "/api/delete?uuid=" + uuid,
                type: "GET",
                async: true,        //异步
                processData: false,  //很重要，告诉jquery不要对form进行处理
                contentType: false,  //很重要，指定为false才能形成正确的Content-Type
                success: function (data) {
                    Swal.fire("成功", "删除成功", "success").then(function (isConfirm) {
                        location.reload(true);
                    });

                }, error: function (XMLHttpRequest, errorThrown) {
                    Swal.fire("失败", "删除失败", "error").then(function (isConfirm) {
                        location.reload(true);
                    });
                }
            });
        } else {
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