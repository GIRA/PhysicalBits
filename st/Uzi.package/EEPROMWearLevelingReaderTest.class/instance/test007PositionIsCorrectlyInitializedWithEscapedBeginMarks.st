tests
test007PositionIsCorrectlyInitializedWithEscapedBeginMarks
	| eeprom reader |
	eeprom := EEPROM size: 256.
	eeprom write: 32 value: begin.
	eeprom write: 64 value: begin.
	eeprom write: 65 value: begin.
	eeprom write: 216 value: end.
	eeprom write: 218 value: begin.
	reader := EEPROMWearLevelingReader on: eeprom.
	self assert: 33 equals: reader position
	