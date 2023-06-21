let ToastMessageBox = (function () {
	let css = {
		info: {"textStyle":"text-dark", "icon":"fa-circle-info"},
		success: {"textStyle":"text-success", "icon":"fa-circle-check"},
		error: {"textStyle":"text-danger", "icon":"fa-circle-xmark"},
		warning: {"textStyle":"text-warning", "icon":"fa-triangle-exclamation"},
	};

	function getToastId(toastContainer) {
		return "toast-msg-box-" + (toastContainer.childNodes.length? parseInt(toastContainer.lastChild.getAttribute("id").split("-")[3])+1 : 1);
	}

	function build(data) {
	let toastContainer = $("#toast-container").get(0);
	let newMsgBox = $("#toast-template").get(0).cloneNode(true);
	let toastId = getToastId(toastContainer);
	newMsgBox.setAttribute("id", toastId);
	newMsgBox.classList.add(css[data.type]["textStyle"])

	// Header
	newMsgBox.children[0].children[0].classList.add(css[data.type]["icon"])
	newMsgBox.children[0].children[1].textContent = data.type.charAt(0).toUpperCase() + data.type.substr(1).toLowerCase();

	// Body
	newMsgBox.children[1].textContent = data.text;

	toastContainer.appendChild(newMsgBox);
	newMsgBox.timeoutId = setTimeout(() => hide(newMsgBox), data.delay? data.delay : 10000);
	return toastId;
	}

	function hide(newMsgBox) {
		newMsgBox.classList.add("hide");
		if (newMsgBox.timeoutId) clearTimeout(newMsgBox.timeoutId)
		setTimeout(() => newMsgBox.remove(), 1000);
	}

	function show(data) {
		console.log(data);
		let _id = build(data);
		$("#" + _id).toast("show");
	}

	return {
		show: show
	};
})();



