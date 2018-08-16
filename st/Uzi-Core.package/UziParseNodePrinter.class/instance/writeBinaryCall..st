visiting
writeBinaryCall: node
	self write: '('.
	node arguments 
		do: [:arg | self visit: arg] 
		separatedBy: [self write: ' {1} ' format: { node selector }].
	self write: ')'
	