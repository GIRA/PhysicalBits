as yet unclassified
test001PositionIsCorrectlyInitializedOnAnEmptyEEPROM
	| eeprom writer |
	eeprom := EEPROM size: 256.
	writer := EEPROMWearLevelingWriter on: eeprom.
	self assert: begin equals: (eeprom read: 0).
	self assert: 1 equals: writer position