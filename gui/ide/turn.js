let TurnNotifier = (function () {

  let activeSubmission = null;
  let hiding = false; // TODO(Richo): This sucks!

  function formatDuration(durationSeconds) {
    let seconds = Math.floor(durationSeconds % 60);
    let minutes = Math.floor(durationSeconds / 60);
    let hours = Math.floor(durationSeconds / 3600);
    let result = seconds.toString();
    if (seconds < 10) { result = "0" + result; }
    result = minutes + ":" + result;
    if (minutes < 10) { result = "0" + result; }
    if (hours > 0) {
      result = hours + ":" + result;
    }
    return result;
  }

  function startCountdownTimer() {
    if (!activeSubmission) return;
    let msPassed = Date.now() - activeSubmission.testBeginTime;
    let msRemaining = activeSubmission.testDuration - msPassed;
    let secondsRemaining = msRemaining / 1000;
    $("#turn-notifier-timer").text(formatDuration(secondsRemaining));
    $("#turn-notifier-timer").css("color", secondsRemaining < 10 ? "red" : "inherit");
    setTimeout(startCountdownTimer, 1000);
  }

  function updateGUI() {
    let submission = activeSubmission;
    if (!submission) return;

    $("#turn-notifier-start-button").prop("disabled", submission.state == "RUNNING");
    $("#turn-notifier-pause-button").prop("disabled", submission.state != "RUNNING");
    if (submission.state == "READY") {
      $("#turn-notifier-stop-button").hide();
      $("#turn-notifier-cancel-button").show();
    } else {
      $("#turn-notifier-stop-button").show();
      $("#turn-notifier-cancel-button").hide();
    }
  }

  function isActive(submission) {
    return submission.state == "READY" || submission.state == "RUNNING" || submission.state == "PAUSED";
  }

  function isFinished(submission) {
    return submission.state == "COMPLETED" || submission.state == "CANCELED";
  }

  function showModal() {
    if (hiding) {
      $('#turn-notifier-modal').one('hidden.bs.modal', function (e) {
        hiding = false;
        $("#turn-notifier-modal").modal("show");
      });
    } else {
      $("#turn-notifier-modal").modal("show");
    }
  }

  function hideModal() {
    hiding = true;
    $("#turn-notifier-modal").modal("hide");
  }

  function init() {

    $("#turn-notifier-start-button").on("click", () => Mendieta.start(activeSubmission));
    $("#turn-notifier-pause-button").on("click", () => Mendieta.pause(activeSubmission));
    $("#turn-notifier-stop-button").on("click", () => Mendieta.stop(activeSubmission));
    $("#turn-notifier-cancel-button").on("click", () => Mendieta.stop(activeSubmission));

    // TODO(Richo): THIS SUCKS! I need to find a more elegant way of working around the async modal transitions

    $('#turn-notifier-modal').on('hidden.bs.modal', function (e) {
      hiding = false;
    });

    Mendieta.on("submission-update", submission => {
      if (isActive(submission)) {
        activeSubmission = submission;
        showModal();
        startCountdownTimer();
      } else if (isFinished(submission)) {
        // TODO(Richo): If the activeSubmission is not set but we got here then it must mean one of our pending
        // submissions got canceled, should we show a message?
        if (activeSubmission && activeSubmission.id == submission.id) {
          activeSubmission = null;
          hideModal();
        }
      }
      updateGUI();
    });
  }

  return {
    init: init,
  }
})();
