visiting
visitProgramNode: node
	self saveIntervalFor: node
		during: [
			node imports ifNotEmpty: [:imports |
				imports do: [:each | self visit: each; writeLine: ''].
				self writeLine: ''].
			(node globals 
				reject: [:global |  node imports 
					anySatisfy: [:import | global name beginsWith: import alias , '.']])
				ifNotEmpty: [:globals |
					globals do: [:each | self visit: each; writeLine: ';'].
					self writeLine: ''].
			(node scripts
				reject: [:script |  node imports 
					anySatisfy: [:import | script name beginsWith: import alias , '.']])
				do: [:each | self visit: each]
				separatedBy: [self writeLine: ''; writeLine: '']]