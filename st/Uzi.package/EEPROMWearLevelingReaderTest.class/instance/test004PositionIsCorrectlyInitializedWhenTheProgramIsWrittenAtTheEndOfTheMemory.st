tests
test004PositionIsCorrectlyInitializedWhenTheProgramIsWrittenAtTheEndOfTheMemory
	| eeprom reader |
	eeprom := EEPROM size: 216.
	eeprom write: 32 value: begin.
	eeprom write: 215 value: end.
	reader := EEPROMWearLevelingReader on: eeprom.
	self assert: 33 equals: reader position
	