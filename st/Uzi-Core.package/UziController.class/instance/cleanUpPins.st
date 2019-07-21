stepping
cleanUpPins
	| now |
	now := Time millisecondClockValue.
	(self pins select: [:pin | pin shouldStopMonitoring: now])
		do: [:pin |
			self setPin: pin name report: false.
			self pins removeKey: pin name ifAbsent: ["Do nothing"]]