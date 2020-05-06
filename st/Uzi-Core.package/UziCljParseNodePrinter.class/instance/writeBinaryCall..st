visiting
writeBinaryCall: node
	self writeLine: '(ast/call-node "{1}"' format: { node selector }.
	self indent: 1
		during: [
			self indent; write: '['.			
			node arguments 
				do: [:arg | 
					self write: '(ast/arg-node '.
					arg key	ifNotNil: [:name |
						self write: '"{1}" ' format: { name }].
					self visit: arg value.
					self write: ')'] 
				separatedBy: [self writeLine: ''; indent].
			self write: '])']