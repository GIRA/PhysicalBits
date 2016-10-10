initialize-release
initialize
	super initialize.
	writer := UziEEPROMWearLevelingWriter 
		eeprom: eeprom 
		position: self findPosition.
	beginPosition := writer position.
	self writeBeginMark