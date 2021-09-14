
let Mendieta = (function () {

  let student = null;
  let socket = null;
  let observers = {
    "activity-update": [],
    "submission-update": [],
  };

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

  function start(submission) {
    return new Promise((res, rej) => {
      $.ajax({
        url: "/submissions/" + submission.id + "/start",
        type: "POST",
        success: res,
        error: rej
      });
    });
  }

  function pause(submission) {
    return new Promise((res, rej) => {
      $.ajax({
        url: "/submissions/" + submission.id + "/pause",
        type: "POST",
        success: res,
        error: rej
      });
    });
  }

  function stop(submission) {
    return new Promise((res, rej) => {
      $.ajax({
        url: "/submissions/" + submission.id + "/stop",
        type: "POST",
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
      const evt = JSON.parse(msg.data);
      (observers[evt.type] || []).forEach(fn => {
        try {
          fn(evt.data);
        } catch (err) {
          console.error(err);
        }
      });
    }
  }

  function on(key, fn) {
    observers[key].push(fn);
  }

  return {
    registerStudent: registerStudent,
    connectToServer: connectToServer,
    on: on,
    submit: submit,
    start: start,
    pause: pause,
    stop: stop,
  }
})();
