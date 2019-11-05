assertions
assertPrimitive: node
	^ self assert: node isPrimitiveDeclaration
		description: 'Primitive declaration expected'
		node: node