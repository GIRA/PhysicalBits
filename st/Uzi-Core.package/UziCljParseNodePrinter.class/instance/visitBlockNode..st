visiting
visitBlockNode: node
	node statements
		ifEmpty: [self write: '(ast/block-node [])']
		ifNotEmpty: [
			self writeLine: '(ast/block-node '.
			self indent: 1
				during: [
					self indent.						
					self write: '['.
					self indent: 1 
						during: [node statements 
							do: [:stmt | self visit: stmt]
							separatedBy: [self writeLine: ''; indent]].
					self write: ']'.
					self write: ')']]