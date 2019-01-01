as yet unclassified
nextInstructionsForStepOver
	| current |
	current := self currentInstruction.
	current ifNil: [^ #()].
	current isBranch ifTrue: [^ self nextStatementsFromBranch].
	current isReturn ifTrue: [^ self nextStatementsFromReturn].
	^ self nextStatements