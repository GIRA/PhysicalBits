/**
 * Wrapper around bootstrap-notify.js
 * http://goodybag.github.io/bootstrap-notify/
 */
var Alert = (function () {
	
	function node() {
		var node = $(".notifications");
		if (node.length === 0) {
			// If no div found, it creates one by default at the
			// top-right corner and appends it to the document.
			node = $("<div>")
				.addClass("notifications")
				.addClass("top-right");
			$(document.body).append(node);
		}
		return node;
	}
	
	function show(msg, type) {
		node().notify({
			message: { text: msg },
			type: type
		}).show();
	}
	
	return {
		info: function (msg) {
			show(msg, "info");
		},
		success: function (msg) {
			show(msg, "success");
		},
		warning: function (msg) {
			show(msg, "warning");
		},
		danger: function (msg) {
			show(msg, "danger");
		}		
	};
})();