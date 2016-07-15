as yet unclassified
test007PositionIsCorrectlyInitializedEvenAfterEscapedEndAtTheBoundaries
	| eeprom writer |
	eeprom := EEPROM size: 256.
	eeprom write: 0 value: end.
	eeprom write: 255 value: end.
	eeprom write: 32 value: end.
	
	writer := EEPROMWearLevelingWriter on: eeprom.	
	self assert: 0 equals: (eeprom read: 32).
	self assert: begin equals: (eeprom read: 33).
	self assert: 34 equals: writer position