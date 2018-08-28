visiting
visitScriptPauseNode: node
	self saveIntervalFor: node during: [
		self write: 'pause '.
		node scripts 
			do: [:each | self write: each]
			separatedBy: [self write: ', ']]