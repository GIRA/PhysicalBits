config
programRange
	^ config at: #programRange ifAbsent: [32 to: 128]