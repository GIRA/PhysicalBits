config
readerClass
	^ config at: #readerClass ifAbsent: [EEPROMWearLevelingReader]