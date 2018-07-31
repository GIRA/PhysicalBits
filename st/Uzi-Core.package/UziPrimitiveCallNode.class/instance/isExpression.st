testing
isExpression
	^ self primitive
		ifNil: true
		ifNotNil: [:prim | prim isExpression]