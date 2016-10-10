stepping
useTheEEPROM
	| program |
	started ifFalse: [^ self].
	program := (1 to: self programRange atRandom) collect: [:n | 256 atRandom - 1].
	(self write: program)
		ifTrue: [self verify: program.
			programs := programs + 1.
			programsSize := programsSize + program size]
		ifFalse: [started := false]