function json2select(json) {
    var html = "";
    var first=true
    for (var key in json) {
        html += "<li onclick=getWecomQR(this," + key + ") class='qr-code-corp-label ";
        if (first) {
            html += " active ";
            first = false;
        }
        html += "'>" + json[key].name + "</li>";
    }
    return html;
}

function getWecomQR(obj, idx) {
    if (obj !== null ) {
        $('ul li.active').removeClass('active');
        obj.className = 'qr-code-corp-label active';
    } else {
    // 如果没有传入 obj，默认选择第一个}
        $('ul li:first').addClass('active');
    }
    if (idx == null) {
        idx = 0;
    }
    if (localStorage.getItem("corpInfo") == null) {
        getCorpInfo();
    }
    var corpInfo = JSON.parse(localStorage.corpInfo);
    var weParam = corpInfo[idx];
    weParam["id"] = "wx_div";
    // add redirect_uri via current location
    var redirect_uri;
    if (location.port === "") {
        redirect_uri = location.protocol + "//" + location.hostname + "/cas/v1/wecom/callback";
    } else {
        redirect_uri = location.protocol + "//" + location.hostname + ":" + location.port + "/cas/v1/wecom/callback";
    }
    weParam["redirect_uri"] = redirect_uri;
    var params = new URLSearchParams(window.location.search)
    if (params.get('service')) {
        weParam['redirect_uri'] = weParam['redirect_uri'] + '?service=' + encodeURIComponent(params.get('service'));
    }
    console.log("redirect_uri = <" + weParam['redirect_uri'] + ">");
    window.WwLogin(weParam);
}

function getCorpInfo() {
    if (localStorage.corpInfo == null) {
        $.ajax({
            url: '/cas/v1/wecom/corpInfo',
            type: 'GET',
            dataType: 'json',
            contentType: 'application/json',
            async: false,
            success: function (data) {
                $('#corpDiv').html(json2select(data));
                localStorage.corpInfo = JSON.stringify(data);
            }
        });
    } else {
        console.log("get from localStorage");
        var data = JSON.parse(localStorage.corpInfo);
        $('#corpDiv').html(json2select(data));
    }
}

function wxQR() {
    // initial wecom qrcode
    getCorpInfo();
    getWecomQR();
}