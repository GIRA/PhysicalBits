as yet unclassified
test005TheBeginMarkIsEscapedCorrectly
	| eeprom writer |
	eeprom := EEPROM size: 256.
	writer := EEPROMWearLevelingWriter on: eeprom.
	writer nextPut: begin.
	writer close.
	"The result should be:
	#[0 begin begin begin end ...]"
	self assert: 0 equals: (eeprom read: 0).
	1 to: 3 do: [:i | self assert: begin equals: (eeprom read: i)].
	self assert: end equals: (eeprom read: 4)