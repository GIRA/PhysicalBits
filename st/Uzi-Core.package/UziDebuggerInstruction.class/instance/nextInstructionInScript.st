accessing
nextInstructionInScript
	| next |
	next := self stop + 1.
	next > self script stop ifTrue: [next := self script start].
	^ self script instructionAtPC: next