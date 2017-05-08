visiting
visitScriptNode: node
	script := node.

	node tickingRate ifNotNil: [:rate | self assertTickingRate: rate].
	self assertBlock: node body.
	
	node tickingRate ifNotNil: [:rate | self visit: rate].
	self visit: node body