$("#addGraphButton").on("click", function () {
	choosePins(function (selection) {
		console.log(selection);
		for (var i = 0; i < selection.length; i++) {
			Uzi.togglePinReport(selection[i]);
		}
	});	
});

function choosePins(callback) {
	$("#monitorSelectionModal #acceptButton").on("click", function () {
		var selected = [];
		$("#monitorSelectionModal input:checked").each(function () { 
			selected.push(parseInt(this.value));
		});
		callback(selected);
		$("#monitorSelectionModal").modal('hide');
	});
	$("#monitorSelectionModal").on("hide.bs.modal", function () {
		$("#monitorSelectionModal #acceptButton").off("click");
		$("#monitorSelectionModal").off("hide.bs.modal");
	});
	$("#monitorSelectionModal input:checkbox").each(function () { 
		this.checked = false; 
	});
	$("#monitorSelectionModal").modal({});
}

Uzi.onMonitorUpdate(function () {	
	var str = "";
	Uzi.pins.forEach(function (pin) {
		str += "PIN " + pin.number;
		pin.history.slice(-10).forEach(function (each) {
			str += "<br>\t" + each.timestamp + " -> " + each.value;
		});
		str += "<br><br>";
	});
	$("#editor").html(str);
});