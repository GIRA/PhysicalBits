visiting
visitProgramNode: node
	self saveIntervalFor: node
		during: [
			node imports ifNotEmpty: [:imports |
				imports do: [:each | self visit: each; writeLine: ''].
				self writeLine: ''].
			node globals ifNotEmpty: [:globals |
				globals do: [:each | self visit: each; writeLine: ';'].
				self writeLine: ''].
			node scripts 
				do: [:each | self visit: each]
				separatedBy: [self writeLine: ''; writeLine: '']]