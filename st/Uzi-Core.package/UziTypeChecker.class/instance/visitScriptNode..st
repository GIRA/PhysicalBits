visiting
visitScriptNode: node
	self assert: (node tickingRate isNil or: [node tickingRate isTickingRate])
		description: 'Ticking rate expected' 
		node: node tickingRate.
	self assert: node body isBlock
		description: 'Block expected'
		node: node body.
	node tickingRate ifNotNil: [:rate | self visit: rate].
	self visit: node body