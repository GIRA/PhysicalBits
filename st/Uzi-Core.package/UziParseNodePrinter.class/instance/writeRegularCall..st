visiting
writeRegularCall: node
	self write: node selector.
	self write: '('.
	node arguments 
		do: [:arg | self visit: arg] 
		separatedBy: [self write: ', '].
	self write: ')'
	