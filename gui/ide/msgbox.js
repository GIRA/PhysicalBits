let MessageBox = (function () {
  let icons = {
    info: "fas fa-info-circle",
    warning: "fas fa-exclamation-circle",
    question: "fas fa-question-circle",
  }
  let nop = () => {};
  let accept = nop;
  let cancel = nop;

  $("#msg-box-accept-button").on("click", function() {
    accept();
    hide();
  });
  $("#msg-box-cancel-button").on("click", function() {
    cancel();
    hide();
  });
  $("#msg-box-modal").on('hidden.bs.modal', function (e) {
    cancel();
    accept = cancel = nop;
  });
  $('#msg-box-modal').on('keypress', function (event) {
    var keycode = (event.keyCode ? event.keyCode : event.which);
    if(keycode == '13'){
      accept();
      hide();
    }
  });

  function config(title, message, icon) {
    $("#msg-box-title").text(title);
    $("#msg-box-message").text(message);
    $("#msg-box-icon").attr("class", icon);
  }

  function show() {
    $("#msg-box-modal").modal();
  }

  function hide() {
    $("#msg-box-modal").modal("hide");
  }

  function alert(title, message, icon) {
    return new Promise((resolve, reject) => {
      try {
        cancel();
        config(title, message, icon || icons.warning);
        $("#msg-box-accept-button").show();
        $("#msg-box-cancel-button").hide();
        $("#msg-box-input").hide();
        accept = cancel = resolve;
        show();
      } catch (err) {
        reject(err);
      }
    });
  }

  function confirm(title, message, icon) {
    return new Promise((resolve, reject) => {
      try {
        cancel();
        config(title, message, icon || icons.question);
        $("#msg-box-accept-button").show();
        $("#msg-box-cancel-button").show();
        $("#msg-box-input").hide();
        accept = () => resolve(true);
        cancel = () => resolve(false);
        show();
      } catch (err) {
        reject(err);
      }
    });
  }

  function prompt(title, message, defaultValue, icon) {
    return new Promise((resolve, reject) => {
      try {
        cancel();
        config(title, message, icon || icons.question);
        $("#msg-box-accept-button").show();
        $("#msg-box-cancel-button").show();
        $("#msg-box-input").val(defaultValue || "").show();
        accept = () => resolve($("#msg-box-input").val());
        cancel = () => resolve(undefined);
        show();
      } catch (err) {
        reject(err);
      }
    });
  }

  return {
    ICONS: icons,
    alert: alert,
    confirm: confirm,
    prompt: prompt,
  };
})();
