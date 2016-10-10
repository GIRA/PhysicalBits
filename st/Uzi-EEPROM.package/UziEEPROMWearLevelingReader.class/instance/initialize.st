initialize-release
initialize
	super initialize.
	reader := UziEEPROMReader eeprom: eeprom position: self findPosition.
	self omitBeginMark