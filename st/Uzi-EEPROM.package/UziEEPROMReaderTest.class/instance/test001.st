as yet unclassified
test001
	| eeprom reader |
	eeprom := UziEEPROM size: 256.
	0 to: 255 do: [:i | eeprom write: i value: i].
	reader := UziEEPROMReader on: eeprom.
	0 to: 255 do: [:i | self assert: i equals: reader next].
	self assert: 0 equals: reader position