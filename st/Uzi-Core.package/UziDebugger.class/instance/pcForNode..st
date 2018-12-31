accessing
pcForNode: node
	program instructions withIndexDo: [:instr :index |
		instr node = node ifTrue: [^ index - 1]].
	^ nil