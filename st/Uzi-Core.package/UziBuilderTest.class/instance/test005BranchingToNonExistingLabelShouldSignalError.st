tests
test005BranchingToNonExistingLabelShouldSignalError
	self should: [Uzi 
			program: [:p | p
				script: #blink
				ticking: true 
				delay: 500
				instructions: [:s | s
					jmp: #a]]]
		raise: UziLabelNotFoundError 