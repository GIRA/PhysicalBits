accessing
linesOfCode
	| src allInstructions |
	src := self sourceCode.
	allInstructions := self instructions.
	^ Array streamContents: [:stream |
		src lineIndicesDo: [:start :ign :end || line interval instructions pc |
			line := src copyFrom: start to: end.
			interval := start to: end.
			instructions := allInstructions
				select: [:instr | instr node notNil 
					and: [instr node token notNil
					and: [instr node token collection = src
					and: [instr node interval first >= interval first
					and: [instr node interval last <= interval last]]]]].
			pc := instructions ifEmpty: [nil]
				ifNotEmpty: [(allInstructions identityIndexOf: instructions first) - 1].
			stream nextPut: (line -> pc)]]
