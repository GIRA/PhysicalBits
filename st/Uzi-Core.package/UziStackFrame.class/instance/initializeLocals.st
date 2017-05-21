accessing
initializeLocals
	locals := Dictionary new.
	script variables withIndexDo: [:var :index |
		(stack at: fp + index ifAbsent: [nil])
			ifNotNil: [:value | locals at: var put: value]]