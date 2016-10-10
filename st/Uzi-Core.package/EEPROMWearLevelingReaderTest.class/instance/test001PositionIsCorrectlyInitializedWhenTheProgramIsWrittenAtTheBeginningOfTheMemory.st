tests
test001PositionIsCorrectlyInitializedWhenTheProgramIsWrittenAtTheBeginningOfTheMemory
	| eeprom reader |
	eeprom := UziEEPROM size: 16.
	eeprom write: 0 value: begin.
	eeprom write: 8 value: end.
	reader := UziEEPROMWearLevelingReader on: eeprom.
	self assert: 1 equals: reader position.
	self deny: reader atEnd 