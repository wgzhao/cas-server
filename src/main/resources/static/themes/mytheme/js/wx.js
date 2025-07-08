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
    var appid =  weParam['appid'];
    var agentid = weParam['agentid'];
    var params = new URLSearchParams(window.location.search)
    // set the appid to the cookie
    document.cookie = "cur=" + idx + "; path=/; SameSite=Lax";
    // clear previous login panel if exists
    $('#wx_div').empty();
    var request_path = '/cas/v1/wecom/callback';
    ww.createWWLoginPanel({
      el: "#wx_div",
      params: {
          login_type: 'CorpApp',
          appid: appid,
          agentid: agentid,
          redirect_uri: location.protocol + "//" + location.host + request_path,
          state: 'WWLogin',
          redirect_type: 'callback',
          panel_size: 'small',
      },
      onLoginSuccess({ code }) {
            console.log({ code })
            var service = params.get('service') || location.href;
              $.ajax({
                url: `${request_path}?code=${code}&corpId=${appid}&service=${encodeURIComponent(service)}`,
                type: 'GET',
                dataType: 'json',
                contentType: 'application/json',
                success: function(data) {
                    if (data.code == 200) {
                        const st = data.data.ticket;
                        if (service.indexOf('?') > -1) {
                            service = service + '&ticket=' + st;
                        } else {
                            service = service + '?ticket=' + st;
                        }
                        location.href = service;
                    }
                    else if (data.code = 601) {
                        location.href = data.data.bindingUrl;
                    } else {
                        alert("登录失败，" + data.msg);
                    }
                },
                error: function (xhr, textStatus, errorThrown) {
                      console.error("Failed to fetch corpInfo:", textStatus, errorThrown);
                  }
                });
          },
     });
}

function getCorpInfo() {
        $.ajax({
            url: '/cas/v1/wecom/corpInfo',
            type: 'GET',
            dataType: 'json',
            contentType: 'application/json',
            async: false,
            success: function (data) {
                $('#corpDiv').html(json2select(data));
                localStorage.corpInfo = JSON.stringify(data);
            },
             error: function (xhr, textStatus, errorThrown) {
                  console.error("Failed to fetch corpInfo:", textStatus, errorThrown);
              }
        });
    }

function wxQR() {
    getCorpInfo();
    var idx = 0;
    if (document.cookie.indexOf("cur") > -1) {
        // 如果设置 currCorp cookie，那就直接使用用户上一次的选择
        idx = document.cookie.match(/cur=(\d+)/)[1];
    }
    var ele = $("#corpDiv").children("li")[idx] || null;
    getWecomQR(ele, idx);
}