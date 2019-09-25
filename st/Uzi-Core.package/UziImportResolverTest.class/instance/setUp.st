private
setUp
	ir := UziImportResolver 
		parser: UziParser new  
		librariesPath: (Uzi defaultDirectory / 'uzi' / 'tests') pathName.

	program := UziProgramNode.
	varDec := UziVariableDeclarationNode.
	proc := UziProcedureNode.
	assignment := UziAssignmentNode.
	var := UziVariableNode.
	number := UziNumberLiteralNode.
	block := UziBlockNode.
	conditional := UziConditionalNode.
	task := UziTaskNode.
	until := UziUntilNode.
	doUntil := UziDoUntilNode.
	start := UziScriptStartNode.
	stop := UziScriptStopNode.
	import := UziImportNode.
	call := UziCallNode.
	func := UziFunctionNode.
	return := UziReturnNode.
	repeat := UziRepeatNode.
	pause := UziScriptPauseNode.
	resume := UziScriptResumeNode.
	yield := UziYieldNode.
	forever := UziForeverNode.
	for := UziForNode.
	pin := UziPinLiteralNode.
	tickingRate := UziTickingRateNode