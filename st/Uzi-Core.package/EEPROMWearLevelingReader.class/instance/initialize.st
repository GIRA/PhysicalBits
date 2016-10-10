initialize-release
initialize
	super initialize.
	reader := EEPROMReader eeprom: eeprom position: self findPosition.
	self omitBeginMark