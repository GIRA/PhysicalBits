initialize-release
initialize
	super initialize.
	writer := EEPROMWriter eeprom: eeprom position: self findPosition.
	beginPosition := writer position.
	self writeBeginMark