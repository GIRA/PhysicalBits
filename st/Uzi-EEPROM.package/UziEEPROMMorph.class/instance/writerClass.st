config
writerClass
	^ config at: #writerClass ifAbsent: [UziEEPROMWearLevelingWriter]