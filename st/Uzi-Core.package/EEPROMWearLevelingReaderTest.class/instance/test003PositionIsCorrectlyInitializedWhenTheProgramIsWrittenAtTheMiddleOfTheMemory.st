tests
test003PositionIsCorrectlyInitializedWhenTheProgramIsWrittenAtTheMiddleOfTheMemory
	| eeprom reader |
	eeprom := UziEEPROM size: 256.
	eeprom write: 32 value: begin.
	eeprom write: 215 value: end.
	reader := UziEEPROMWearLevelingReader on: eeprom.
	self assert: 33 equals: reader position.
	self deny: reader atEnd 
	