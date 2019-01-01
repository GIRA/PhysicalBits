accessing
branchInstruction
	^ self script 
		instructionAtPC: self stop + instructions last argument + 1
		ifAbsent: [nil]