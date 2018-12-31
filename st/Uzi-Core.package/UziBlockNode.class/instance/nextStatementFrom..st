accessing
nextStatementFrom: node
	| index |
	index := (self children indexOf: node) + 1.
	^ self children 
		at: index
		ifPresent: [:next | { next }]
		ifAbsent: [parent nextStatements]