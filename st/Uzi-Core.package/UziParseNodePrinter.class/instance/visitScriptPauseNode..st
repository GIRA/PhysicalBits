visiting
visitScriptPauseNode: node
	self write: 'pause '.
	node scripts 
		do: [:each | self visit: each]
		separatedBy: [self write: ', ']