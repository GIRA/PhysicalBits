stepping
cleanUpGlobals
	| now |
	now := Time millisecondClockValue.
	(self globals select: [:global | global shouldStopMonitoring: now])
		do: [:global |
			self setGlobal: global name report: false.
			self globals removeKey: global name ifAbsent: ["Do nothing"]]