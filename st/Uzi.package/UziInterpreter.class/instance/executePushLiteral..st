bytecodes
executePushLiteral: argument
	stack push: (script literalAt: argument)