as yet unclassified
test001PositionIsCorrectlyInitializedOnAnEmptyEEPROM
	| eeprom writer |
	eeprom := EEPROM size: 256.
	writer := EEPROMWearLevelingWriter on: eeprom.
	self assert: 0 equals: (eeprom read: 0).
	self assert: begin equals: (eeprom read: 1).
	self assert: 2 equals: writer position