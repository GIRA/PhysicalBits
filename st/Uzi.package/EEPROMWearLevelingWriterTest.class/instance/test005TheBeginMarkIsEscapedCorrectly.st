as yet unclassified
test005TheBeginMarkIsEscapedCorrectly
	| eeprom writer |
	eeprom := EEPROM size: 256.
	writer := EEPROMWearLevelingWriter on: eeprom.
	writer nextPut: begin.
	writer close.
	0 to: 2 do: [:i | self assert: begin equals: (eeprom read: i)].
	self assert: end equals: (eeprom read: 3)