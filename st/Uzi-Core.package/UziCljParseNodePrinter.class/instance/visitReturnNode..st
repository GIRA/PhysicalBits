visiting
visitReturnNode: node
	self halt.
	self saveIntervalFor: node
		during: [
			self write: 'return'.
			node value ifNotNil: [:val | 
				self write: ' '; visit: val]]