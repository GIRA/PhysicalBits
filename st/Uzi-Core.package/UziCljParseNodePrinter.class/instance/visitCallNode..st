visiting
visitCallNode: node
	node arguments 
		ifEmpty: [self write: '(ast/call-node "{1}" [])' format: { node selector }]
		ifNotEmpty: [
			self writeLine: '(ast/call-node "{1}"' format: { node selector }.
			self indent: 1
				during: [
					self indent; write: '['.			
					node arguments 
						do: [:arg | 
							(arg isKindOf: Association)
								ifTrue: [
									self write: '(ast/arg-node '.
									arg key	ifNotNil: [:name |
										self write: '"{1}" ' format: { name }].
									self visit: arg value.
									self write: ')']
								ifFalse: [self visit: arg]]
						separatedBy: [self writeLine: ''; indent].
					self write: '])']]