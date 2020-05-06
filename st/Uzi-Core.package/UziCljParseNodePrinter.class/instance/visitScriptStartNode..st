visiting
visitScriptStartNode: node
	self halt.
	self saveIntervalFor: node during: [
		self write: 'start '.
		node scripts 
			do: [:each | self write: each]
			separatedBy: [self write: ', ']]