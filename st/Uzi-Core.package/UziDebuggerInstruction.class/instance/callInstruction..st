accessing
callInstruction: pc
	^ ((instructions withIndexCollect: [:instr :index | instr -> (self start + index - 1)])
		select: [:kvp | kvp value >= pc]
		thenCollect: [:kvp | kvp key])
		detect: [:instr | instr isScriptCall]
		ifNone: [nil]