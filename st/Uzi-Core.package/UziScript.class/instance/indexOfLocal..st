accessing
indexOfLocal: anUziVariable 
	locals withIndexDo: [:each :index |
		each = anUziVariable ifTrue: [^ index - 1]].
	^ nil