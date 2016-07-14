as yet unclassified
test006TheEndMarkIsEscapedCorrectly
	| eeprom writer |
	eeprom := EEPROM size: 256.
	writer := EEPROMWearLevelingWriter on: eeprom.
	writer nextPut: end.
	writer close.
	self assert: begin equals: (eeprom read: 0).
	1 to: 2 do: [:i | self assert: end equals: (eeprom read: i)].
	self assert: end equals: (eeprom read: 3)