let ToastMessageBox = (function () {
	let css = {
		info: {"textStyle":"text-dark", "icon":"fa-circle-info"},
		success: {"textStyle":"text-success", "icon":"fa-circle-check"},
		error: {"textStyle":"text-danger", "icon":"fa-circle-xmark"},
		warning: {"textStyle":"text-warning", "icon":"fa-triangle-exclamation"},
	};

	function removeToast(toast) {
		toast.classList.add("hide");
		if (toast.timeoutId) clearTimeout(toast.timeoutId);
		setTimeout(() => toast.remove(), 1000);
	}

	function getToastId(toastContainer) {
		return "toast-msg-box-" + (toastContainer.childNodes.length? parseInt(toastContainer.lastChild.getAttribute("id").split("-")[3])+1 : 1);
	}

	function buildToast(data){
		const toastContainer = document.querySelector(".notifications");
		const toast = document.createElement("li");
		toast.setAttribute("id", getToastId(toastContainer));
		toast.className = `toast ${data.type}`;
		toast.setAttribute("data-autohide", false);
		toast.innerHTML = `<div class="column">
								<i class="fas ${css[data.type]["icon"]}"></i>
								<span>${data.text}</span>
							</div>
							<i class="fas fa-xmark" onclick="ToastMessageBox.removeToast(this.parentElement)"></i>
							`;
		toastContainer.appendChild(toast);
		toast.timeoutId = setTimeout(() => removeToast(toast), data.delay? data.delay : 5000);
		return toast.id;
	}

	function show(data) {
		console.log(data);
		let _id = buildToast(data);
		$("#" + _id).toast("show");
	}

	return {
		show: show,
		removeToast:removeToast
	};
})();



