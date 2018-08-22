visiting
visitScriptStopNode: node
	self saveIntervalFor: node during: [
		self write: 'stop '.
		node scripts 
			do: [:each | self visit: each]
			separatedBy: [self write: ', ']]