
Uzi.onMonitorUpdate(function () {	
	var str = "";
	Uzi.pins.forEach(function (pin) {
		str += "PIN " + pin.number;
		pin.history.forEach(function (each) {
			str += "<br>\t" + each.timestamp + " -> " + each.value;
		});
		str += "<br><br>";
	});
	$("#editor").html(str);
});