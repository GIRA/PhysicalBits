tests
test005PositionIsCorrectlyInitializedWhenTheProgramSurpassesTheEndOfTheMemory
	| eeprom reader |
	eeprom := UziEEPROM size: 256.
	eeprom write: 32 value: begin.
	eeprom write: 15 value: end.
	reader := UziEEPROMWearLevelingReader on: eeprom.
	self assert: 33 equals: reader position.
	self deny: reader atEnd 
	