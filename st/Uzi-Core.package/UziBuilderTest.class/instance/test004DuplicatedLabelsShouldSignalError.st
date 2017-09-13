tests
test004DuplicatedLabelsShouldSignalError
	self should: [Uzi 
			program: [:p | p
				script: #blink
				ticking: true 
				delay: 500
				instructions: [:s | s
					label: #a;
					label: #a]]]
		raise: UziDuplicatedLabelError