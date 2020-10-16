/*
HACK(Richo): I added this custom script to display the link to the DOWNLOAD page as a button.
There are probably much better ways of doing this but I didn't want to spend much time looking
for a proper solution. I know this decision will probably come back to bite me.
*/
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
if (ul.find(".current").get(0) == undefined) {
	ul.replaceWith(btn);
}
