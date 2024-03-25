function json2select(json) {
    var html = '<select id="corp" onchange="getWecomQR()">';
    for (var key in json) {
        html += '<option value="' + json[key].entityId + '">' + json[key].name + '</option>';
    }
    html += '</select>';
    return html;
}

function getWecomQR() {
    var corpid = $('#corp').val();
    console.log(corpid);
    var corpInfo = JSON.parse(localStorage.corpInfo);
    var weParam = corpInfo[corpid];
    weParam["id"] = "wx_gp51";
    // add redirect_uri via current location
    var redirect_uri;
    if (location.port == "") {
        redirect_uri = location.protocol + "//" + location.hostname + "/cas/v1/wecom/callback";
    } else {
        redirect_uri = location.protocol + "//" + location.hostname + ":" + location.port + "/cas/v1/wecom/callback";
    }
    weParam["redirect_uri"] = redirect_uri;
    var params = new URLSearchParams(window.location.search)
    if (params.get('service')) {
        weParam['redirect_uri'] = weParam['redirect_uri'] + '?service=' + params.get('service');
    }
    console.log(weParam);
    window.WwLogin(weParam);
}

function getCorpInfo() {
    if (localStorage.corpInfo == null) {
        $.ajax({
            url: '/cas/v1/wecom/corpInfo',
            type: 'GET',
            dataType: 'json',
            contentType: 'application/json',
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
    var params = new URLSearchParams(window.location.search)
    var username = params.get('username')
    if (username != null) {
        $("#username").val(username);
        $("#password").val(params.get('password'));
        // then, submit the form
        $("#fm1").submit();
    } else {
        // initial wecom qrcode
        getCorpInfo();
        getWecomQR()
    }
}