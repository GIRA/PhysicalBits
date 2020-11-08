/*
HACK(Richo): I added this custom script to make a few changes to the page. There
are probably much better ways of doing this but I didn't want to spend much time
looking for a proper solution. This decision will probably come back to bite me.
*/

/* Display the link to the DOWNLOAD page as a button. */
var btn = $("<div>")
	.css("margin", "10px 15px")
	.append($("<a>")
		.addClass("h4")
		.addClass("btn")
		.addClass("btn-large")
		.addClass("btn-block")
		.css("color", "black")
		.css("background-color", "orange")
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

/* Increase the font size of the title */
$(".title a").removeClass("h4").addClass("h2");
$(".title a i").css("font-size", "inherit");

/* Hide the search bar */
$(".search").css("display", "none");
