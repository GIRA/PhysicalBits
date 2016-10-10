tests
test008PositionIsCorrectlyInitializedWithEscapedEndMarks
	| eeprom reader |
	eeprom := UziEEPROM size: 256.
	eeprom write: 32 value: begin.
	eeprom write: 64 value: begin.
	eeprom write: 65 value: begin.
	eeprom write: 216 value: end.
	eeprom write: 217 value: end.
	eeprom write: 219 value: end.
	reader := UziEEPROMWearLevelingReader on: eeprom.
	self assert: 33 equals: reader position.
	self deny: reader atEnd 
	