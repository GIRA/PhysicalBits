as yet unclassified
test001
	| eeprom writer |
	eeprom := EEPROM size: 256.
	writer := EEPROMWriter on: eeprom.
	0 to: 255 do: [:i | writer nextPut: i].
	0 to: 255 do: [:i | self assert: i equals: (eeprom read: i)].
	self assert: 0 equals: writer position