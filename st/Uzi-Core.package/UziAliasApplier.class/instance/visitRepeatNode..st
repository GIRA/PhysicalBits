visiting
visitRepeatNode: node
	^ (node class 
		times: (self visit: node times)
		body: (self visit: node body)) 
		token: node token