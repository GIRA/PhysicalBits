visiting
visitScriptStopNode: node
	self write: 'stop '.
	node scripts 
		do: [:each | self visit: each]
		separatedBy: [self write: ', ']