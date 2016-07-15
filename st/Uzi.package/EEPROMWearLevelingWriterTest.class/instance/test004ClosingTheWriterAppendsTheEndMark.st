as yet unclassified
test004ClosingTheWriterAppendsTheEndMark
	| eeprom writer |
	eeprom := EEPROM size: 256.
	writer := EEPROMWearLevelingWriter on: eeprom.
	0 to: 5 do: [:i | writer nextPut: i].
	writer close.
	"The result should be:
	#[0 begin 0 1 2 3 4 5 end ...]"
	self assert: nil equals: writer position.
	self assert: 0 equals: (eeprom read: 0).
	self assert: begin equals: (eeprom read: 1).
	0 to: 5 do: [:i | self assert: i equals: (eeprom read: i + 2)].
	self assert: end equals: (eeprom read: 8)