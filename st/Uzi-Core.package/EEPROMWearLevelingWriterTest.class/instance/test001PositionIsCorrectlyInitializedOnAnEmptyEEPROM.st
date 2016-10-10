as yet unclassified
test001PositionIsCorrectlyInitializedOnAnEmptyEEPROM
	| eeprom writer |
	eeprom := UziEEPROM size: 256.
	writer := UziEEPROMWearLevelingWriter on: eeprom.
	self assert: 0 equals: (eeprom read: 0).
	self assert: begin equals: (eeprom read: 1).
	self assert: 2 equals: writer position