stepping
stepOverOn: debugger
	^ Array with: self nextInstructionInScript 
		with: self branchInstruction