accessing
pcForInstruction: instr
	program instructions withIndexDo: [:each :index |
		each == instr ifTrue: [^ index - 1]].
	^ nil