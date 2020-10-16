var btn = $("<div>")
	.css("margin", "10px 15px")
	.append($("<a>")
		.addClass("btn")
		.addClass("btn-large")
		.addClass("btn-block")
		.css("color", "black")
		.attr("href", "/PhysicalBits/DOWNLOAD.html")
		.attr("type", "button")
		.append($("<i>")
			.addClass("fa")
			.addClass("fa-download")
			.addClass("mx-2"))
		.append($("<span>")
			.addClass("text-uppercase")
			.text("Download")));

var ul = $(".toctree :first-child").first();
if (ul.get(0).className.search("current") == -1) {
	ul.replaceWith(btn);
}
