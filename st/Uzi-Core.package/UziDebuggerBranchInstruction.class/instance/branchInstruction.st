as yet unclassified
branchInstruction
	| next |
	next := self stop + instructions last argument + 1.
	(next < self script start or: [next > self script stop]) 
		ifTrue: [next := self script start].
	^ self script instructionAtPC: next