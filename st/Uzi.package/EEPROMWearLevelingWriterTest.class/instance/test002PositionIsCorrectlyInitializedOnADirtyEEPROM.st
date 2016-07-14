as yet unclassified
test002PositionIsCorrectlyInitializedOnADirtyEEPROM
	| eeprom writer |
	eeprom := EEPROM size: 256.
	eeprom write: 15 value: begin.
	eeprom write: 20 value: begin.
	eeprom write: 32 value: end.
	
	writer := EEPROMWearLevelingWriter on: eeprom.
	self assert: begin equals: (eeprom read: 32).
	self assert: 33 equals: writer position