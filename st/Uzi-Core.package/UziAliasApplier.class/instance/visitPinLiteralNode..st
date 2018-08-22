visiting
visitPinLiteralNode: node
	^ (node class 
		type: node type 
		number: node number) 
		token: node token