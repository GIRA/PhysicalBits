visiting
visitReturnNode: node
	^ (node class 
		value: (node value ifNotNil: [:value | self visit: value])) 
		token: node token