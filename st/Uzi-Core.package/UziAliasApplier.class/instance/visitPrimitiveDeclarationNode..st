visiting
visitPrimitiveDeclarationNode: node
	^ (node class
		alias: alias , node alias
		name: node name)
		token: node token