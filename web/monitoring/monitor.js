var UziMonitor = (function () {

	var monitors = [];

	function init() {
		$("#addGraphButton").on("click", function () {
			addMonitor();
		});

		Uzi.onMonitorUpdate(function () {
			monitors.forEach(function (monitor) {
				var colors = palette('rainbow', Uzi.pins.length + Uzi.globals.length);
				var min = 0;
				var max = 0;
				var pins = Uzi.pins.filter(function (pin) {
					return monitor.pins.indexOf(pin.number) !== -1;
				});
				var pins_ds = pins.map(function (pin) {
					var i = Uzi.pins.findIndex(function (each) {
						return each.number == pin.number;
					});
					var color = "#" + colors[i];
					var dataset = monitor.chart.data.datasets.find(function (each) {
						return each.id === pin.number;
					});
					var oldData = dataset === undefined ? [] : dataset.data;
					var newData = pin.history.map(function (each) {
						var timestamp = each.timestamp;
						if (timestamp > max) {
							max = timestamp;
							min = max - 10000;
						}
						return {
							x: timestamp,
							y: each.value
						};
					});
					if (newData.length > 0) {
						oldData = oldData.filter(function (each) {
							return each.x < newData[0].x;
						});
					}
					return {
						id: pin.number,
						label: "Pin " + pin.name,
						fill: false,
						borderColor: color,
						backgroundColor: color,
						pointRadius: 0,
						data: oldData.concat(newData).filter(function (each) {
							return each.x >= min && each.x <= max;
						})
					}
				});
				var globals = Uzi.globals.filter(function (global) {
					return monitor.globals.indexOf(global.name) !== -1;
				});
				var globals_ds = globals.map(function (global) {
					var i = Uzi.globals.indexOf(global) + Uzi.pins.length;
					var color = "#" + colors[i];
					var dataset = monitor.chart.data.datasets.find(function (each) {
						return each.id === global.name;
					});
					var oldData = dataset === undefined ? [] : dataset.data;
					var newData = global.history.map(function (each) {
						var timestamp = each.timestamp;
						if (timestamp > max) {
							max = timestamp;
							min = max - 10000;
						}
						return {
							x: timestamp,
							y: each.value
						};
					});
					if (newData.length > 0) {
						oldData = oldData.filter(function (each) {
							return each.x < newData[0].x;
						});
					}
					return {
						id: global.name,
						label: "Global " + global.name,
						fill: false,
						borderColor: color,
						backgroundColor: color,
						pointRadius: 0,
						data: oldData.concat(newData).filter(function (each) {
							return each.x >= min && each.x <= max;
						})
					}
				});
				var data = {
					datasets: pins_ds.concat(globals_ds)
				};
				var options = {
					maintainAspectRatio: false,
					showTooltips: false,
					animation: {
						duration: 0
					},
					scales: {
						xAxes: [{
							display: false,
							type: 'linear',
							position: 'bottom',
							ticks: {
								min: min,
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
				monitor.chart.data = data;
				monitor.chart.options = options;
				monitor.chart.update();
			});
		});
	}

	function addMonitor() {
		choosePins(function (selection) {
			buildLineChartFor(selection);
			updatePinsReporting();
			updateGlobalsReporting();
		});
	}

	function removeMonitor(monitor) {
		var index = monitors.indexOf(monitor);
		monitors.splice(index, 1);
		monitor.container.remove();
		updatePinsReporting();
		updateGlobalsReporting();
	}

	function updatePinsReporting() {
		var pins = new Set();
		monitors.forEach(function (monitor) {
			monitor.pins.forEach(function (pin) {
				pins.add(pin);
			});
		});
		pins.forEach(function (pin) { Uzi.activatePinReport(pin); });
		var toRemove = [];
		Uzi.pinsReporting.forEach(function (pin) {
			if (!pins.has(pin)) { toRemove.push(pin); }
		});
		toRemove.forEach(function (pin) { Uzi.deactivatePinReport(pin); });
	}

	function updateGlobalsReporting() {
		var globals = new Set();
		monitors.forEach(function (monitor) {
			monitor.globals.forEach(function (global) {
				globals.add(global);
			});
		});
		globals.forEach(function (global) { Uzi.activateGlobalReport(global); });
		var toRemove = [];
		Uzi.globalsReporting.forEach(function (global) {
			if (!globals.has(global)) { toRemove.push(global); }
		});
		toRemove.forEach(function (global) { Uzi.deactivateGlobalReport(global); });
	}

	function buildLineChartFor(selection) {
		var editor = $("#editor");
		var container = $("<div>").addClass("monitor");
		var closeButton = $("<button>")
			.attr("type", "button")
			.attr("aria-label", "Close")
			.css("position", "absolute")
			.css("right", "10px")
			.addClass("close")
			.append($("<span>")
				.attr("aria-hidden", "true")
				.html("&times;"))
			.on("click", function () {
				removeMonitor(monitor)
			});
		var canvas = $("<canvas>");
		container.draggable({
			containment: "parent",
			scroll: true,
			snap: true
		});
		container.resizable({
			minHeight: 150,
			minWidth: 200,
			handles: "n, e, s, w, ne, se, sw, nw"
		});

		// HACK(Richo): JQuery UI seems to add the "position:relative"
		container.attr("style", null);

		container.append(closeButton);
		container.append(canvas);
		editor.append(container);

		var chart = createChartOn(canvas.get(0));
		var monitor = {
			container: container,
			pins: selection.digitalPins.concat(selection.analogPins),
			globals: selection.globals,
			chart: chart,
		};
		monitors.push(monitor);
	}

	function choosePins(callback) {
		var digitalPins1 = [
			{ text: "D2", value: 2 },
			{ text: "D3", value: 3 },
			{ text: "D4", value: 4 },
			{ text: "D5", value: 5 },
			{ text: "D6", value: 6 },
			{ text: "D7", value: 7 },
		];
		var digitalPins2 = [
			{ text: "D8", value: 8 },
			{ text: "D9", value: 9 },
			{ text: "D10", value: 10 },
			{ text: "D11", value: 11 },
			{ text: "D12", value: 12 },
			{ text: "D13", value: 13 },
		];
		var analogPins = [
			{ text: "A0", value: 14 },
			{ text: "A1", value: 15 },
			{ text: "A2", value: 16 },
			{ text: "A3", value: 17 },
			{ text: "A4", value: 18 },
			{ text: "A5", value: 19 },
		];
		var globals = Uzi.availableGlobals.map(function (g) {
			return { text: g.name, value: g.number };
		});
		var modal = $("<div>")
			.addClass("modal")
			.addClass("fade")
			.attr("tabindex", "-1")
			.attr("role", "dialog")
			.attr("aria-labelledby", "monitorSelection")
			.append($("<div>")
				.addClass("modal-dialog")
				.attr("role", "document")
				.append($("<div>")
					.addClass("modal-content")
					.append($("<div>")
						.addClass("modal-header")
						.append($("<button>")
							.addClass("close")
							.attr("type", "button")
							.attr("data-dismiss", "modal")
							.attr("aria-label", "Close")
							.append($("<span>")
								.attr("aria-hidden", "true")
								.html("&times;")))
						.append($("<h4>")
							.addClass("modal-title")
							.attr("id", "monitorSelection")
							.text("Choose pins to monitor")))
					.append($("<div>")
						.addClass("modal-body")
						.append($("<h5>").text("Digital pins:"))
						.append($("<p>")
							.append(digitalPins1.map(function (each) {
								return $("<label>")
									.addClass("checkbox-inline")
									.append($("<input>")
										.attr("type", "checkbox")
										.attr("name", "digitalPins")
										.attr("value", each.value))
									.append($("<span>").text(each.text));
							})))
						.append($("<p>")
							.append(digitalPins2.map(function (each) {
								return $("<label>")
									.addClass("checkbox-inline")
									.append($("<input>")
										.attr("type", "checkbox")
										.attr("name", "digitalPins")
										.attr("value", each.value))
									.append($("<span>").text(each.text));
							})))
						.append("<hr>")
						.append($("<h5>").text("Analog pins:"))
						.append($("<p>")
							.append(analogPins.map(function (each) {
								return $("<label>")
									.addClass("checkbox-inline")
									.append($("<input>")
										.attr("type", "checkbox")
										.attr("name", "analogPins")
										.attr("value", each.value))
									.append($("<span>").text(each.text));
							})))
						.append("<hr>")
						.append($("<h5>").text("Global variables:"))
						.append($("<p>")
							.append(globals.map(function (each) {
								return $("<label>")
									.addClass("checkbox-inline")
									.append($("<input>")
										.attr("type", "checkbox")
										.attr("name", "globals")
										.attr("value", each.text))
									.append($("<span>").text(each.text));
							}))))
					.append($("<div>")
						.addClass("modal-footer")
						.append($("<button>")
							.addClass("btn")
							.addClass("btn-default")
							.attr("type", "button")
							.attr("data-dismiss", "modal")
							.text("Cancel"))
						.append($("<button>")
							.addClass("btn")
							.addClass("btn-primary")
							.attr("type", "button")
							.text("Accept")
							.on("click", function () {
								var selected = {
									digitalPins: [],
									analogPins: [],
									globals: []
								};
								modal.find("input[name=digitalPins]").each(function () {
									if (this.checked) {
										selected.digitalPins.push(parseInt(this.value));
									}
								});
								modal.find("input[name=analogPins]").each(function () {
									if (this.checked) {
										selected.analogPins.push(parseInt(this.value));
									}
								});
								modal.find("input[name=globals]").each(function () {
									if (this.checked) {
										selected.globals.push(this.value);
									}
								});
								callback(selected);
								modal.modal('hide');
							})))));
		modal.on("hidden.bs.modal", function () {
			modal.remove();
		});
		$(document.body).append(modal);
		modal.modal({});
	}

	function createChartOn(canvas) {
		var ctx = canvas.getContext('2d');
		var data = {};
		var options = {};
		return new Chart(ctx, {
			type: 'line',
			data: data,
			options: options
		});
	}

	return {
		init: init,
		monitors: monitors
	};
})();
