accessing
sourceCodeInterval
	| intervals filtered |
	filtered := innerInstructions select: [:each | each node notNil].
	filtered size > 1
		ifTrue: [filtered := filtered select: [:each | each isBranch not]].
	intervals := filtered collect: [:each | each node interval].
	intervals ifEmpty: [^ nil].
	^ (intervals collect: [:each | each first]) min
		to: (intervals collect: [:each | each last]) max