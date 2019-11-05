visiting
visitPrimitiveDeclarationNode: node
	self assert: node alias isString
		description: 'Invalid alias'
		node: node.
	self assert: node name isString
		description: 'Invalid name'
		node: node.
	self assert: (UziPrimitive primSpec anySatisfy: [:prim | prim first = node name])
		description: 'Primitive not found'
		node: node