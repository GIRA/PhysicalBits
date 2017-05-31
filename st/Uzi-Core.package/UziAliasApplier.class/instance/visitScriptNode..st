visiting
visitScriptNode: node
	script := node.
	^ (node class 
		name: alias , node name
		state: node state
		tickingRate: (node tickingRate ifNotNil: [:tr | self visit: tr])
		body: (self visit: node body))
		token: node token