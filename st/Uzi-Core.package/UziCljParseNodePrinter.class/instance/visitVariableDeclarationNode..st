visiting
visitVariableDeclarationNode: node
	self writeLine: '(ast/variable-declaration-node'.
	self indent: 1
		during: [
			self indent; writeLine: '"{1}"' format: { node name }.
			node value 
				ifNil: [self indent; write: '(ast/literal-number-node 0)']
				ifNotNil: [:val | self indent; visit: node value].
			self write: ')']