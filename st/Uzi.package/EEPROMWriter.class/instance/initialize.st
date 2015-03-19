initializing
initialize
	super initialize.
	position := self firstEmptyPage.
	eeprom write: position value: 255.
	position := position + 8 \\ eeprom size.
	self nextPut: 254.