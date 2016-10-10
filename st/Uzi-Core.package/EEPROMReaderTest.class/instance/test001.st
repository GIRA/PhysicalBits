as yet unclassified
test001
	| eeprom reader |
	eeprom := EEPROM size: 256.
	0 to: 255 do: [:i | eeprom write: i value: i].
	reader := EEPROMReader on: eeprom.
	0 to: 255 do: [:i | self assert: i equals: reader next].
	self assert: 0 equals: reader position