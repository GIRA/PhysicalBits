tests
testBranchingToNonExistingLabelShouldSignalError
	self should: [Uzi 
			program: [:p | p
				script: #blink
				ticking: true 
				delay: 500
				instructions: [:s | s
					jmp: #a]]]
		raise: UziLabelNotFoundError 