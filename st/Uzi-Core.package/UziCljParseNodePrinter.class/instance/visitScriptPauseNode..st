visiting
visitScriptPauseNode: node
	self halt.
	self saveIntervalFor: node during: [
		self write: 'pause '.
		node scripts 
			do: [:each | self write: each]
			separatedBy: [self write: ', ']]