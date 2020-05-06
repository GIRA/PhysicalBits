visiting
writeTask: node
	self writeLine: '(ast/task-node '.
	self indent: 1
		during: [
			self indent;
				writeLine: ':name "{1}"' format: { node name asString }.
			self indent;
				writeLine: ':state "{1}"' format: { 
					node state asString
				}.
				
			node tickingRate 
				ifNotNil: [:tr | self indent; write: ':tick-rate '; visit: tr; writeLine: ''].
			self indent;
				write: ':body ';
				visit: node body;
				write: ')']