stepping
cleanUpGlobals
	| now |
	now := Time millisecondClockValue.
	(self globals select: [:global | global shouldStopMonitoring: now])
		do: [:global |
			self setGlobal: global number report: false.
			self globals removeKey: global number ifAbsent: ["Do nothing"]]