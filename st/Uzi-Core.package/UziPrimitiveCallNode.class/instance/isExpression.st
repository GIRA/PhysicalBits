testing
isExpression
	^ primitive 
		ifNil: true
		ifNotNil: [:prim | prim isExpression ]