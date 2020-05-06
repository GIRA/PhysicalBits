visiting
writeUnaryCall: node
	self halt.
	self write: node selector; 
		visit: node arguments first value