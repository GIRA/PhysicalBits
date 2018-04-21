visiting
visitForNode: node
	^ (node class
		counter: (self visit: node counter)
		start: (self visit: node start)
		stop: (self visit: node stop)
		step: (self visit: node step) 
		body: (self visit: node body))
			token: node token