private
setUp
	ir := UziImportResolver 
		parser: UziParser new  
		librariesPath: (Uzi gitDirectory / 'uzi' / 'tests') pathName.

	program := UziProgramNode.
	varDec := UziVariableDeclarationNode.
	proc := UziProcedureNode.
	assignment := UziAssignmentNode.
	var := UziVariableNode.
	prim := UziPrimitiveCallNode.
	number := UziNumberNode.
	block := UziBlockNode.
	conditional := UziConditionalNode.
	task := UziTaskNode.
	until := UziUntilNode.
	doUntil := UziDoUntilNode.
	start := UziScriptStartNode.
	stop := UziScriptStopNode.
	import := UziImportNode.
	call := UziScriptCallNode.
	func := UziFunctionNode.
	return := UziReturnNode.
	ref := UziScriptRefNode.
	repeat := UziRepeatNode.
	pause := UziScriptPauseNode.
	resume := UziScriptResumeNode.
	yield := UziYieldNode.
	forever := UziForeverNode.
	for := UziForNode 