visiting
visitReturnNode: node
	^ (node class 
		value: (self visit: node value)) 
		token: node token