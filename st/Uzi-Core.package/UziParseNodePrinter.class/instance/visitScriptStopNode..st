visiting
visitScriptStopNode: node
	self saveIntervalFor: node during: [
		self write: 'stop '.
		node scripts 
			do: [:each | self write: each]
			separatedBy: [self write: ', ']]