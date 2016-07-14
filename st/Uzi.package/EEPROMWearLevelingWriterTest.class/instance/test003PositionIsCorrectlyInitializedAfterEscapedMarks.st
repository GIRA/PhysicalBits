as yet unclassified
test003PositionIsCorrectlyInitializedAfterEscapedMarks
	| eeprom writer |
	eeprom := EEPROM size: 256.
	eeprom write: 15 value: begin.
	eeprom write: 16 value: begin.
	eeprom write: 20 value: end.	
	eeprom write: 21 value: end.
	eeprom write: 30 value: end.	
	eeprom write: 31 value: end.
	eeprom write: 32 value: end.
	
	writer := EEPROMWearLevelingWriter on: eeprom.
	self assert: begin equals: (eeprom read: 32).
	self assert: 33 equals: writer position