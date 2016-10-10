as yet unclassified
test009PositionIsCorrectlyInitializedIfTheMarkIsAtTheEnd
	| eeprom writer |
	eeprom := UziEEPROM size: 8.
	eeprom write: 7 value: end.
	
	writer := UziEEPROMWearLevelingWriter on: eeprom.	
	self assert: 0 equals: (eeprom read: 7).
	self assert: begin equals: (eeprom read: 0).
	self assert: 1 equals: writer position