
let Mendieta = (function () {

  let student = null;
  let socket = null;
  let currentActivity = null;
  let observers = [];

  function registerStudent(data) {
    return new Promise((res, rej) => {
      $.ajax({
        url: "/students",
        type: "POST",
        data: data,
        success: result => {
          student = result;
          res(student);
        },
        error: rej
      });
    });
  }

  function submit(program) {
    return new Promise((res, rej) => {
      $.ajax({
        url: "/submissions",
        type: "POST",
        data: {
          author: student,
          program: JSONX.stringify(program)
        },
        success: res,
        error: rej
      });
    });
  }

  function connectToServer() {
    // TODO(Richo): Handle server disconnect gracefully
    let url = "ws://" + location.host + "/updates";
    socket = new WebSocket(url);

    socket.onerror = function (err) {
      console.error(err);
    }
    socket.onopen = function () {
      socket.send(student.id);
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
    registerStudent: registerStudent,
    connectToServer: connectToServer,
    onUpdate: onUpdate,
    submit: submit,
  }
})();
