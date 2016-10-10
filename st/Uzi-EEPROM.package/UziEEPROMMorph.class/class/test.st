instance creation
test
	"
	self test openInHand.
	"
	| config |
	config := {
		#eepromSize -> 512.
		#programRange -> (0 to: 256).
		#readerClass -> UziEEPROMWearLevelingReader.
		#writerClass -> UziEEPROMWearLevelingWriter
	} as: Dictionary.
	^ self config: config