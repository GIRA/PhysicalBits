as yet unclassified
test011WritingAfterTheBeginPositionThrowsException
	| eeprom writer |
	eeprom := EEPROM size: 8.
	"0    1     2 3 4 5 6 7
	0 begin 1 1 1 1 1 1"
	writer := EEPROMWearLevelingWriter on: eeprom.	
	6 timesRepeat: [writer nextPut: 1].
	self should: [writer nextPut: 2] raise: AssertionFailure 