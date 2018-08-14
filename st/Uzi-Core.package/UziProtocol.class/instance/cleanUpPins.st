stepping
cleanUpPins
	| now |
	now := Time millisecondClockValue.
	(self pins select: [:pin | pin shouldStopMonitoring: now])
		do: [:pin |
			self setPin: pin number report: false.
			self pins removeKey: pin number ifAbsent: ["Do nothing"]]