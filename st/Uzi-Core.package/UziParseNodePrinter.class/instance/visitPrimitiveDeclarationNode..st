visiting
visitPrimitiveDeclarationNode: node
	self saveIntervalFor: node during: [
		self write: 'prim '.
		node alias = node name
			ifTrue: [self write: node name]
			ifFalse: [self write: node alias; write: ' : '; write: node name].
		self write: ', ']