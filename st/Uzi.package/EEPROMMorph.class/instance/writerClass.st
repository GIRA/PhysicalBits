config
writerClass
	^ config at: #writerClass ifAbsent: [EEPROMWearLevelingWriter]