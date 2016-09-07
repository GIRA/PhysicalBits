literals
scriptLiteral
	^ super scriptLiteral ==> [:scriptName | UziScriptRefNode named: scriptName]