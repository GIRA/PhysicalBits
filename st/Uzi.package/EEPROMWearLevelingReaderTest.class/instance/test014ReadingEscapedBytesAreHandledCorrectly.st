tests
test014ReadingEscapedBytesAreHandledCorrectly
	| eeprom reader |
	eeprom := EEPROM size: 256.
	eeprom write: 14 value: begin.
	eeprom write: 15 value: begin.	
	eeprom write: 16 value: begin.
	eeprom write: 17 value: 1.
	eeprom write: 18 value: 2.
	eeprom write: 19 value: 3.
	eeprom write: 20 value: end.
	eeprom write: 21 value: end.
	eeprom write: 22 value: 4.
	eeprom write: 32 value: end.
	reader := EEPROMWearLevelingReader on: eeprom.
	self assert: 15 equals: reader position.
	self assert: begin equals: reader next.
	self assert: 1 equals: reader next.
	self assert: 2 equals: reader next.
	self assert: 3 equals: reader next.
	self assert: end equals: reader next.
	self assert: 4 equals: reader next.
	self deny: reader atEnd.