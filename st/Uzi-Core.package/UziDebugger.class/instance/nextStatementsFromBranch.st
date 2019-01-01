as yet unclassified
nextStatementsFromBranch
	| current next nextPC |
	current := self currentInstruction.
	nextPC := pc + current argument.
	[nextPC := nextPC + 1.
	next := self instructionAtPC: nextPC]
		doWhileFalse: [next isStatement].
	^ self nextStatements, { next }