visiting
visitScriptNode: node
	(program 
		script: node name 
		ticking: node state = #running
		delay: (node tickingRate ifNil: [0] ifNotNil: [:tickingRate | tickingRate delayMs]) 
		instructions: [:s |
			script := s.
			node arguments do: [:var | script registerArgument: var name].
			self visit: node body.
			script := nil])
		node: node