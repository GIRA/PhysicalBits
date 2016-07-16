tests
test010PositionIsCorrectlyInitializedWhenTheEEPROMIsFullOfEndMarks
	| eeprom reader |
	eeprom := EEPROM size: 256.
	0 to: eeprom size - 1 do: [:i | eeprom write: i value: end].
	reader := EEPROMWearLevelingReader on: eeprom.
	self assert: 1 equals: reader position.
	self assert: reader atEnd 
	