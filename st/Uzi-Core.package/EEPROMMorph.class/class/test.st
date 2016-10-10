instance creation
test
	"
	self test openInHand.
	"
	| config |
	config := {
		#eepromSize -> 512.
		#programRange -> (0 to: 256).
		#readerClass -> EEPROMWearLevelingReader.
		#writerClass -> EEPROMWearLevelingWriter
	} as: Dictionary.
	^ self config: config