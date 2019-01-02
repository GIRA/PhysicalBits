accessing
branchInstruction
	^ self script 
		instructionAtPC: self stop + innerInstructions last argument + 1
		ifAbsent: [nil]