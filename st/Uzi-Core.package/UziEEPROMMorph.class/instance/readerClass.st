config
readerClass
	^ config at: #readerClass ifAbsent: [UziEEPROMWearLevelingReader]