var UziMonitor = (function () {
	
	var chart;
	
	function init() {
		chart = testChart();
		
		$("#addGraphButton").on("click", function () {
			choosePins(function (selection) {
				console.log(selection);
				for (var i = 0; i < selection.length; i++) {
					Uzi.togglePinReport(selection[i]);
				}
			});	
		});

		Uzi.onMonitorUpdate(function () {		
			console.log(Uzi.pins);
			var colors = palette('rainbow', Uzi.pins.length);
			var max = 0;
			var data = { 
				datasets: Uzi.pins.map(function (pin, i) {
					return {
						label: "Pin " + pin.number,
						fill: false,
						borderColor: "#" + colors[i],
						backgroundColor: "#" + colors[i],
						//pointBorderWidth: 1,
						pointRadius: 0,
						data: pin.history.map(function (each) {
							timestamp = each.timestamp;
							if (timestamp > max) {
								max = timestamp;
							}
							return {
								x: timestamp,
								y: each.value
							};
						})
					}
				})
			};
			var options = {
				title: {
					display: true,
					text: 'ACAACA Nombre a elegir por el usuario?'
				},
				tooltips: {
					mode: 'index',
					intersect: false,				
				},
				hover: {
					mode: 'nearest',
					intersect: true,
				},
				animation: {
					duration: 0
				},
				scales: {
					xAxes: [{
						display: false,
						type: 'linear',
						position: 'bottom',
						ticks: {
							min: max - 10000,
							max: max
						}
					}]
				},
				elements: {
					line: {
						tension: 0,
					}
				}
			};
			chart.data = data;
			chart.options = options;
			chart.update();
		});
	}
	

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
	
	function testChart() {		
		var ctx = document.getElementById("myChart").getContext('2d');
		var data = {};
		var options = {};
		return new Chart(ctx, {
			type: 'line',
			data: data,
			options: options
		});
	}

	return {
		init: init
	};
})();

