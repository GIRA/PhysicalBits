building
program: aBlock
	| builder |
	builder := UziProgramBuilder new.
	aBlock value: builder.
	^ builder program