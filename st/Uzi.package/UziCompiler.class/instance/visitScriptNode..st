visiting
visitScriptNode: node
	program 
		script: node name 
		ticking: node state = #ticking
		delay: (node tickingRate ifNil: [0] ifNotNil: [:tickingRate | tickingRate delayMs]) 
		instructions: [:s |
			script := s.
			self visitCollection: node body]