visiting
visitBlockNode: node
	node statements 
		do: [:each | self assertStatement: each].
	
	self visitCollection: node statements