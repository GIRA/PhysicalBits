initializing
initialize
	super initialize.
	eeprom := UziEEPROM size: self eepromSize.
	self extent: eeprom size @ 500;
		color: Color white.
	lastChanged := Time millisecondClockValue.
	started := true.
	programs := programsSize := 0.