tests
test002PositionIsCorrectlyInitializedOnAnEmptyEEPROM
	| eeprom reader |
	eeprom := UziEEPROM size: 256.
	reader := UziEEPROMWearLevelingReader on: eeprom.
	self assert: 1 equals: reader position.
	self deny: reader atEnd 
	