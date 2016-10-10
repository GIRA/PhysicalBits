tests
test009PositionIsCorrectlyInitializedWhenTheEEPROMIsFullOfBeginMarks
	| eeprom reader |
	eeprom := UziEEPROM size: 256.
	0 to: eeprom size - 1 do: [:i | eeprom write: i value: begin].
	reader := UziEEPROMWearLevelingReader on: eeprom.
	self assert: 1 equals: reader position.
	self deny: reader atEnd 
	