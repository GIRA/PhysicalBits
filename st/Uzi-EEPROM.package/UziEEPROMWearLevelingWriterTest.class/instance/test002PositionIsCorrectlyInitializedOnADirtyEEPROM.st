as yet unclassified
test002PositionIsCorrectlyInitializedOnADirtyEEPROM
	| eeprom writer |
	eeprom := UziEEPROM size: 256.
	eeprom write: 15 value: begin.
	eeprom write: 20 value: begin.
	eeprom write: 32 value: end.
	
	writer := UziEEPROMWearLevelingWriter on: eeprom.
	self assert: 0 equals: (eeprom read: 32).
	self assert: begin equals: (eeprom read: 33).
	self assert: 34 equals: writer position