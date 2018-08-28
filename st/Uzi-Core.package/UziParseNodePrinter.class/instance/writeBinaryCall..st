visiting
writeBinaryCall: node
	self write: '('.
	node arguments 
		do: [:arg |
			arg key ifNotNil: [:name |
				self write: '{1}: ' format: { name }].
			self visit: arg value] 
		separatedBy: [self write: ' {1} ' format: { node selector }].
	self write: ')'
	