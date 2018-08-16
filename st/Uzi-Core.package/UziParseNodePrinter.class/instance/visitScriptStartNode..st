visiting
visitScriptStartNode: node
	self write: 'start '.
	node scripts 
		do: [:each | self visit: each]
		separatedBy: [self write: ', ']