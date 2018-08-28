visiting
visitScriptResumeNode: node
	self saveIntervalFor: node during: [
		self write: 'resume '.
		node scripts 
			do: [:each | self write: each]
			separatedBy: [self write: ', ']]