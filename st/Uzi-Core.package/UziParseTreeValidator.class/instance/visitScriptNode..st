visiting
visitScriptNode: node
	| locals args |
	node tickingRate ifNotNil: [:rate | self assertTickingRate: rate].
	self assertBlock: node body.
	
	args := Set new.
	node arguments do: [:arg |
		self assert: (args includes: arg name) not
			description: 'Argument name alredy specified'
			node: arg.
		args add: arg name].
	
	locals := Set new.
	node locals do: [:local |
		self assert: (node arguments allSatisfy: [:arg | arg name ~= local name])
			description: 'Variable already in use as script argument'
			node: local.
		self assert: (locals includes: local name) not
			description: 'Variable already declared'
			node: local.
		locals add: local name].
	
	node tickingRate ifNotNil: [:rate | self visit: rate].
	self visit: node body