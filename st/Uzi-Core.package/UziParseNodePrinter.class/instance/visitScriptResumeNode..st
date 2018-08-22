visiting
visitScriptResumeNode: node
	self saveIntervalFor: node during: [
		self write: 'resume '.
		node scripts 
			do: [:each | self visit: each]
			separatedBy: [self write: ', ']]