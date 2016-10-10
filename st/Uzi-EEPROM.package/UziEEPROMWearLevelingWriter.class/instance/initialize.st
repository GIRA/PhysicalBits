initialize-release
initialize
	super initialize.
	writer := UziEEPROMWriter 
		eeprom: eeprom 
		position: self findPosition.
	beginPosition := writer position.
	self writeBeginMark