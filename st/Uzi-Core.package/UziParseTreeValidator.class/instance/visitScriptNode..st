visiting
visitScriptNode: node
	| args |
	node tickingRate ifNotNil: [:rate | self assertTickingRate: rate].
	self assertBlock: node body.
	
	args := Set new.
	node arguments do: [:arg |
		self assert: (args includes: arg name) not
			description: 'Argument name alredy specified'
			node: arg.
		args add: arg name].
	
	node tickingRate ifNotNil: [:rate | self visit: rate].
	self visitCollection: node arguments;
		visit: node body