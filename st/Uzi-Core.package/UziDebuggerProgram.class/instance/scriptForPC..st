accessing
scriptForPC: pc
	^ (self instructionAtPC: pc) ifNotNil: [:instr | instr script]