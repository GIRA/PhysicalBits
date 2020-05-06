visiting
visitPrimitiveDeclarationNode: node
	(node alias isNil
		or: [node alias = node name])
		ifTrue: [self write: '(ast/primitive-node "{1}")' format: { node name }]
		ifFalse: [self write: '(ast/primitive-node "{1}" "{2}")' 
			format: { node alias . node name }]