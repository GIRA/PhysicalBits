as yet unclassified
test008PositionIsCorrectlyInitializedEvenAfterEscapedEndAtTheBoundaries
	| eeprom writer |
	eeprom := EEPROM size: 16.
	eeprom write: 0 value: end.
	eeprom write: 7 value: end.
	eeprom write: 8 value: end.
	eeprom write: 14 value: end.
	eeprom write: 15 value: end.

	
	writer := EEPROMWearLevelingWriter on: eeprom.	
	self assert: 0 equals: (eeprom read: 0).
	self assert: begin equals: (eeprom read: 1).
	self assert: 2 equals: writer position