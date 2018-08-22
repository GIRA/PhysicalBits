visiting
visitScriptStartNode: node
	self saveIntervalFor: node during: [
		self write: 'start '.
		node scripts 
			do: [:each | self visit: each]
			separatedBy: [self write: ', ']]