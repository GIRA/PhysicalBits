installing
run: anUziProgram
	runningProgram := anUziProgram.
	globalsReporting := globalsReporting intersection: self variables.
	debugger reset.
	[| bytes |
	bytes := self send: { self class msgOutSetProgram }, (Uzi encode: anUziProgram).
	self updateReportingFor: anUziProgram.
	^ bytes]
		ensure: [self triggerEvent: UziEvents programUpdate]