visiting
visitReturnNode: node
	self write: 'return'.
	node value 
		ifNotNil: [:val | self 
			write: ' '; 
			visit: val]