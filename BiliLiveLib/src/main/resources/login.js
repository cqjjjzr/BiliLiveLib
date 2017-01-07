document.getElementById("login-submit").onclick = function() {
		var self = $(this)
          , username = $("#login-username").val()
          , password = $("#login-passwd").val()
          , captcha = $("#login-captcha").val()
          , remember = $("#keep-login").attr("checked") ? 1 : 0;
        if (self.hasClass("disabled") || self.hasClass("loading")) {
            return
        }
        $("#login .input").removeClass("ok error");
        $("#login .message").text("");
        self.addClass("loading");
        $.getJSON("/login?act=getkey&_=" + new Date().getTime(), function(keyObject) {
            if (keyObject && keyObject.error) {
                window.status = "_keyerr " + keyObject.error;
            } else {
                var encrypt = new JSEncrypt();
                encrypt.setPublicKey(keyObject.key);
                password = encrypt.encrypt(keyObject.hash + password);
                $.post("/ajax/miniLogin/login", {
                    "userid": username,
                    "pwd": password,
                    "captcha": captcha,
                    "keep": remember
                }, function(resultObject) {
					window.status = JSON.stringify(resultObject);
                }, "json")
            }
        })
    }
