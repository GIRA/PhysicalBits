
var ajax = (function () {

  var pendingRequests = [
    [], // Priority 0 : HIGH
    [], // Priority 1 : NORMAL
    []  // Priority 2 : LOW
  ];
  var activeXHR = []; // Requests that have already been sent to the server
  var maxRequests = 6; // How many requests can be kept active at the same time

  function GET(url, data, priority) {
    return request({
      type: "GET",
      url: url,
      data: data,
      priority: priority || 0
    });
  }

  function POST(url, data, priority) {
    return request({
      type: "POST",
      url: url,
      data: data,
      priority: priority || 0
    });
  }

  function request(req) {
    return new Promise(function (resolve, reject) {
      pushRequest({
        type: req.type || "GET",
        url: req.url,
        data: req.data,
        success: resolve,
        error: reject
      }, req.priority || 0);
    });
  }

  function pushRequest (req, priority) {
    if (priority === undefined) {
      priority = 1;
    }
    pendingRequests[priority].push(req);
    if (activeXHR.length < maxRequests) {
      sendNextRequest();
    }
  }

  function abortAll () {
    pendingRequests = [[], [], []];
    while (activeXHR.length > 0) {
      var req = activeXHR.shift();
      if (req.readyState != 4) req.abort();
    }
  }

  function abortAllWithPriority (priority) {
    pendingRequests[priority] = [];
    var temp = [];
    for (var i = 0; i < activeXHR.length; i++) {
      var req = activeXHR[i];
      if (req.readyState != 4 && req.priority == priority) {
        req.abort();
      } else {
        temp.push(req);
      }
    }
    activeXHR = temp;
  }

  function abortAllWithPriorityLowerThan (priority) {
    var length = pendingRequests.length;
    for (var i = priority + 1; i < length; i++) {
      abortAllWithPriority(i);
    }
  }

  function sendRequestNow (req, priority) {
    var xhr = $.ajax({
      type: req.type,
      url: req.url,
      data: req.data,
      success: function (data, status, xhr) {
        if (isActive(xhr)) {
          req.success(data);
        }
      },
      complete: function (xhr, status) {
        if (isActive(xhr)) {
          removeFromActiveList(xhr);
        }
        sendNextRequest();
      },
      error: function (xhr, status, error) {
        if (isActive(xhr)) {
          req.error(xhr, status, error);
        }
      }
    });
    xhr.priority = priority;
    activeXHR.push(xhr);
  }

  function sendNextRequest() {
    if (activeXHR.length >= maxRequests) return;
    var priority = choosePriorityToServeNow();
    var requests = pendingRequests[priority];
    if (requests.length === 0) return;
    var req = requests.shift();
    sendRequestNow(req, priority);
  }

  function choosePriorityToServeNow() {
    var length = pendingRequests.length;
    for (var i = 0; i < length - 1; i++) {
      if (pendingRequests[i].length > 0) {
        return i;
      }
    }
    return length - 1;
  }

  function isActive(xhr) {
    return activeXHR.indexOf(xhr, 0) >= 0;
  }

  function removeFromActiveList(element) {
    var index = activeXHR.indexOf(element, 0);
    if (index >= 0) {
      activeXHR.splice(index, 1);
    }
  }

  return {
    GET: GET,
    POST: POST,
    request: request,
    abortAll: abortAll,
    abortAllWithPriorityLowerThan: abortAllWithPriorityLowerThan
  };
})();
