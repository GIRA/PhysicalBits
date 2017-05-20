accessing
indexOfLocal: anUziVariable
	arguments withIndexDo: [:each :index |
		each = anUziVariable ifTrue: [^ index - 1]].
	locals withIndexDo: [:each :index |
		each = anUziVariable ifTrue: [^ index - 1 + arguments size]].
	^ nil