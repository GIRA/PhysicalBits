encoding scripts
writeInstructions: instructions
	instructions
		do: [:instr | self visit: instr]
		separatedBy: [self writeLine: ';']