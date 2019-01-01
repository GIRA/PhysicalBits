as yet unclassified
nextStatements
	| instructions index next |
	instructions := self currentScript instructions.
	index := instructions identityIndexOf: self currentInstruction.
	next := (instructions allButFirst: index)
		detect: [:instr | instr isStatement]
		ifNone: [instructions
			detect: [:instr | instr isStatement]
			ifNone: [instructions first]].
	^ { next }