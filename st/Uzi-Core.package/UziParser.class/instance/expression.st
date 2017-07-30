grammar-expressions
expression 
	^ (ws, $! asParser, ws) optional, (binary / expressionNotBinary) 
		map: [:not :expr | not 
			ifNil: [expr] 
			ifNotNil: [UziPrimitiveCallNode 
				selector: #not 
				arguments: { expr } 
				primitive: nil]]
		