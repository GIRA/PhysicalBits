visiting
visitBlockNode: node
	self saveIntervalFor: node
		during: [
			node statements
				ifEmpty: [self write: '{}']
				ifNotEmpty: [
					self writeLine: '{'.
					self indent: 1 
						during: [node statements 
							do: [:stmt | self indent; visit: stmt.
								(self needsSemicolon: stmt) ifTrue: [self write: ';'].
								self writeLine: '']].
					self indent; write: '}']]