accessing
nextInstruction
	^ self script instructionAtPC: self stop + 1 ifAbsent: [nil]