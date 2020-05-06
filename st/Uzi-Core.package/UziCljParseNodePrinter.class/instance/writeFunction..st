visiting
writeFunction: node
	self writeLine: '(ast/function-node '.
	self indent: 1
		during: [
			self indent;
				writeLine: ':name "{1}"' format: { node name asString }.
			node arguments
				ifNotEmpty: [:args |
					self indent; write: ':arguments ['.
					self indent: 1
						during: [args do: [:arg | self visit: arg]
							separatedBy: [self writeLine: ''; indent]].
					self writeLine: ']'].
			self indent;
				write: ':body ';
				visit: node body;
				write: ')']