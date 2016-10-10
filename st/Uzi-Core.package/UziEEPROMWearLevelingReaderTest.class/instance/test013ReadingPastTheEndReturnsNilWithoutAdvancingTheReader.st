tests
test013ReadingPastTheEndReturnsNilWithoutAdvancingTheReader
	| eeprom reader |
	eeprom := UziEEPROM size: 256.
	eeprom write: 14 value: begin.
	eeprom write: 15 value: 1.
	eeprom write: 16 value: 2.
	eeprom write: 17 value: end.
	reader := UziEEPROMWearLevelingReader on: eeprom.
	self assert: 15 equals: reader position.
	self deny: reader atEnd.
	self assert: 1 equals: reader next.
	self assert: 2 equals: reader next.
	self assert: nil equals: reader next.
	self assert: nil equals: reader next.
	self assert: reader atEnd.
	
	