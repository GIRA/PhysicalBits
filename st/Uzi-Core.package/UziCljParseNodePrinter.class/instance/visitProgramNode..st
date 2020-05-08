visiting
visitProgramNode: node
	self writeLine: '(ast/program-node '.
	self indent: 1
		during: [
			node imports ifNotEmpty: [:imports |
				self indent; write: ':imports ['.
					self indent: 1
						during: [imports 
							do: [:each | self visit: each]
							separatedBy: [self writeLine: ''; indent]].
					self writeLine: ']'].
			
			(node globals 
				reject: [:global |  node imports 
					anySatisfy: [:import | global name beginsWith: import alias , '.']])
				ifNotEmpty: [:globals |
					self indent; write: ':globals ['.
					self indent: 1
						during: [globals 
							do: [:each | self visit: each]
							separatedBy: [self writeLine: ''; indent]].
					self writeLine: ']'].
			
			"(node primitives 
				reject: [:prim | node imports 
						anySatisfy: [:import | prim alias beginsWith: import alias , '.']])
				ifNotEmpty: [:primitives |
					self indent; write: ':primitives ['.
					self indent: 1
						during: [primitives 
							do: [:each | self visit: each]
							separatedBy: [self writeLine: ''; indent]].
					self writeLine: ']']."
			
			self indent; write: ':scripts ['.
			self indent: 1
				during: [(node scripts
					reject: [:script |  node imports 
						anySatisfy: [:import | script name beginsWith: import alias , '.']])
					do: [:each | self visit: each]
					separatedBy: [self writeLine: ''; writeLine: ''; indent]].
			self write: '])']