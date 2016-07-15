tests
test002PositionIsCorrectlyInitializedOnAnEmptyEEPROM
	| eeprom reader |
	eeprom := EEPROM size: 256.
	reader := EEPROMWearLevelingReader on: eeprom.
	self assert: 1 equals: reader position
	