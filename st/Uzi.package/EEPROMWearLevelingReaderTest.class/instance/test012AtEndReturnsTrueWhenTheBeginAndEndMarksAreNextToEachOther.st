tests
test012AtEndReturnsTrueWhenTheBeginAndEndMarksAreNextToEachOther
	| eeprom reader |
	eeprom := EEPROM size: 256.
	eeprom write: 14 value: begin.
	eeprom write: 15 value: end.
	reader := EEPROMWearLevelingReader on: eeprom.
	self assert: 15 equals: reader position.
	self assert: reader atEnd
	