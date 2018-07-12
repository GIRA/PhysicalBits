grammar-binary
buildBinaryExpression: selector left: left right: right
	"First, look for some special operators"
	selector = #'&&' ifTrue: [^ UziLogicalAndNode left: left right: right].
	selector = #'||' ifTrue: [^ UziLogicalOrNode left: left right: right].
	
	"If no special operator is found, return a prim call"
	^ UziPrimitiveCallNode
		selector: selector
		arguments: { left . right }
		primitive: (self primitiveForSelector: selector)