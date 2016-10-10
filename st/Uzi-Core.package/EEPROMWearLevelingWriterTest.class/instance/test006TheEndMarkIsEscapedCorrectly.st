as yet unclassified
test006TheEndMarkIsEscapedCorrectly
	| eeprom writer |
	eeprom := EEPROM size: 256.
	writer := EEPROMWearLevelingWriter on: eeprom.
	writer nextPut: end.
	writer close.
	"The result should be:
	#[0 begin end end end ...]"
	self assert: 0 equals: (eeprom read: 0).
	self assert: begin equals: (eeprom read: 1).
	2 to: 3 do: [:i | self assert: end equals: (eeprom read: i)].
	self assert: end equals: (eeprom read: 4)