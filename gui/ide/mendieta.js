
let Mendieta = (function () {

  let socket = null;
  let currentActivity = null;
  let observers = [];

  function submit(author, program) {
    return new Promise((res, rej) => {
      $.ajax({
        url: "/submissions",
        type: "POST",
        data: {
          author: author,
          program: JSONX.stringify(program)
        },
        success: res,
        error: rej
      });
    });
  }

  function connectToServer() {
    let url = "ws://" + location.host + "/updates";
    socket = new WebSocket(url);

    socket.onerror = function (err) {
      console.error(err);
    }
    socket.onopen = function () {
      console.log("OPEN!");
    }
    socket.onmessage = function (msg) {
      currentActivity = JSON.parse(msg.data);
      for (let i = 0; i < observers.length; i++) {
        let fn = observers[i];
        try {
          fn(currentActivity);
        } catch (err) {
          console.error(err);
        }
      }
    }
  }

  function onUpdate(fn) {
    observers.push(fn);
  }

  return {
    connectToServer: connectToServer,
    onUpdate: onUpdate,
    submit: submit,
  }
})();
