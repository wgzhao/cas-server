function wxQR() {
    weParam = {
        "id": "wx_gp51",
        "appid": "wwfbeaf1a16d7f77b4",
        "agentid": "1000087",
        "redirect_uri": "https%3A%2F%2Fgzg.gp51.com%3A8001%2Fcas%2Flogin",
        "state": "11019",
        "href": "",
    }

    var params = new URLSearchParams(window.location.search)
    var code = params.get('code')
    if (code != null) {
        console.log("current code = " + code);
        // ajax post to the wechat token url
        var tokenUrl = "/cas/v1/wecom/getAccessToken";

        // specify content-type
        $.ajax({
            type: "GET",
            url: tokenUrl,
            contentType: "application/json"}).done(function (data, status) {
            if (status === "success") {
                var token = data["access_token"];
                console.log(token);
                $.ajax({
                    type: "GET",
                    url: "/cas/v1/wecom/getUserInfo", 
                    contentType: "application/json",
                    data: { "code": code, "access_token": token }
                }).done(function (data, status) {
                    if (status === "success") {
                        console.log(data);
                        // var UserId = data['UserId'];
                        // fill form with userId as username and fixed password
                        $("#username").val(data['UserId']);
                        $("#password").val(data['password']);
                        // then, submit the form
                        $("#fm1").submit();
                    }
                });
            }
        });
    } else {
        // initial wecom qrcode
        if (params.get('service')) {
            weParam['redirect_uri'] = weParam['redirect_uri'] + '?service=' + params.get('service');
        }
        window.WwLogin(weParam);
    }
}