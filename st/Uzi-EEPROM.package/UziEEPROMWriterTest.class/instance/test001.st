as yet unclassified
test001
	| eeprom writer |
	eeprom := UziEEPROM size: 256.
	writer := UziEEPROMWriter on: eeprom.
	0 to: 255 do: [:i | writer nextPut: i].
	0 to: 255 do: [:i | self assert: i equals: (eeprom read: i)].
	self assert: 0 equals: writer position