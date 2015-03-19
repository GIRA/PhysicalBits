accessing
incrementPosition
	position := position + 1.
	self actualIndex = nextPointer
		ifTrue: [	nextPointer := nextPointer + (eeprom read: self actualIndex).
			self incrementPosition]
