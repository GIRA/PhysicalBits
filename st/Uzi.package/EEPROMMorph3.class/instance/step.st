stepping
step
	| now |
	now := Time millisecondClockValue.
	now - lastChanged > 100
		ifTrue: [self changed.
			lastChanged := now].
	100 timesRepeat: [self useTheEEPROM]