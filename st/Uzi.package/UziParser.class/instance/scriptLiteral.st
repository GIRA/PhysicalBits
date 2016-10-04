literals
scriptLiteral
	^ super scriptLiteral ==> [:name | UziScriptRefNode named: name]