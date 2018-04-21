visiting
visitForeverNode: node
	self assertBlock: node body.
		
	self visit: node body