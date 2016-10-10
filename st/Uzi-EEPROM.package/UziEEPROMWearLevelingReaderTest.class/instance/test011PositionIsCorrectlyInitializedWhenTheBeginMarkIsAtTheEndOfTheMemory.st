tests
test011PositionIsCorrectlyInitializedWhenTheBeginMarkIsAtTheEndOfTheMemory
	| eeprom reader |
	eeprom := UziEEPROM size: 256.
	eeprom write: 255 value: begin.
	eeprom write: 15 value: end.
	reader := UziEEPROMWearLevelingReader on: eeprom.
	self assert: 0 equals: reader position.
	self deny: reader atEnd 
	