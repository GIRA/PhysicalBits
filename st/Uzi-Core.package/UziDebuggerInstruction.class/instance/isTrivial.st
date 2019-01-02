testing
isTrivial
	^ self innerInstructions last isUnconditionalBranch
		and: [self innerInstructions noneSatisfy: [:instr | instr isScriptCall]]