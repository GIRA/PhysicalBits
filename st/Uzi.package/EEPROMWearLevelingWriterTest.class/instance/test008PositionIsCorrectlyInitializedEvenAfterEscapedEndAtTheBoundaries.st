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
	self assert: begin equals: (eeprom read: 0).
	self assert: 1 equals: writer position