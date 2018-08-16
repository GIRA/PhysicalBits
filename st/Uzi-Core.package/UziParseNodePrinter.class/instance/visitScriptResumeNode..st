visiting
visitScriptResumeNode: node
	self write: 'resume '.
	node scripts 
		do: [:each | self visit: each]
		separatedBy: [self write: ', ']