visiting
visitScriptNode: node
	script := node.
	node tickingRate ifNotNil: [:rate | self assertTickingRate: rate].
	self assertBlock: node body.
	script locals do: [:local |
		self assert: (script arguments allSatisfy: [:arg | arg name ~= local name])
			description: 'Variable already in use as script argument'
			node: local].
	
	node tickingRate ifNotNil: [:rate | self visit: rate].
	self visit: node body