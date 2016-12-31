visiting
visitBlockNode: node
	node statements 
		do: [:each | self 
			assert: each isStatement
			description: 'Statement expected' 
			node: each].
	self visitCollection: node statements