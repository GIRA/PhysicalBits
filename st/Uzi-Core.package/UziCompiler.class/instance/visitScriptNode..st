visiting
visitScriptNode: node
	(program 
		script: node name 
		ticking: (node state = #ticking or: [node tickingRate notNil])
		delay: (node tickingRate ifNil: [0] ifNotNil: [:tickingRate | tickingRate delayMs]) 
		args: (node arguments collect: [:var | var name])
		locals: (node locals collect: [:var | var name])
		instructions: [:s |
			script := s.			
			self visit: node body.
			script := nil])
		node: node