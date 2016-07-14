as yet unclassified
test004ClosingTheWriterAppendsTheEndMark
	| eeprom writer |
	eeprom := EEPROM size: 256.
	writer := EEPROMWearLevelingWriter on: eeprom.
	0 to: 5 do: [:i | writer nextPut: i].
	writer close.
	self assert: nil equals: writer position.
	self assert: begin equals: (eeprom read: 0).
	0 to: 5 do: [:i | self assert: i equals: (eeprom read: i + 1)].
	self assert: end equals: (eeprom read: 7)