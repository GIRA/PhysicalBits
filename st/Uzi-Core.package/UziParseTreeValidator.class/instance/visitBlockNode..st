visiting
visitBlockNode: node
	node statements 
		do: [:each | self assertStatement: each].
	
	self visitCollection: node locals.
	self visitCollection: node statements