initialize-release
initialize
	super initialize.
	writer := EEPROMWriter eeprom: eeprom position: self findPosition.
	self writeBeginMark