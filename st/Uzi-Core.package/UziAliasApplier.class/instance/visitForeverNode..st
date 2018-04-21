visiting
visitForeverNode: node
	^ (node class 
		body: (self visit: node body)) 
		token: node token