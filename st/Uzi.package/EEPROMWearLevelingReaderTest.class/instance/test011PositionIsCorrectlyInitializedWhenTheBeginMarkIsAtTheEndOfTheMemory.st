tests
test011PositionIsCorrectlyInitializedWhenTheBeginMarkIsAtTheEndOfTheMemory
	| eeprom reader |
	eeprom := EEPROM size: 256.
	eeprom write: 255 value: begin.
	eeprom write: 15 value: end.
	reader := EEPROMWearLevelingReader on: eeprom.
	self assert: 0 equals: reader position
	