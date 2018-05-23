visiting
visitScriptNode: node
	(program 
		script: node name 
		ticking: (#(running once) includes: node state)
		delay: (node tickingRate ifNil: [0] ifNotNil: [:tickingRate | tickingRate delayMs]) 
		instructions: [:s |
			script := s.
			node arguments 
				do: [:var | script registerArgument: (self registerNameFor: var)].
			self visit: node body.
			(node state = #once)
				ifTrue: [(script stop: node name) node: node].
			script := nil])
		node: node