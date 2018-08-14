stepping
cleanUpPins
	| now toRemove |
	now := Time millisecondClockValue.
	toRemove := Set new.
	pinLastAccess keysAndValuesDo: [:pin :lastAccess |
		now - lastAccess > 10000
			ifTrue: [toRemove add: pin]].
	toRemove do: [:pin |
		self setPin: pin report: false.
		pinValues removeKey: pin.
		pinLastAccess removeKey: pin]