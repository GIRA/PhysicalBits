visiting
visitScriptCallNode: node
	| callArgs scriptArgs |
	self flag: #Richo. "This treats locals as arguments and compiles a push instruction
	for each local. That is clearly a waste of space because the vm should be able to initialize
	these variables itself."
	scriptArgs := (ast scriptNamed: node script name) arguments, 
		(ast scriptNamed: node script name) locals.
	callArgs := node arguments.
	
	"Fix the argument names"
	(callArgs allSatisfy: [:arg | arg key = #value])
		ifTrue: [callArgs := callArgs withIndexCollect: [:arg :index |
				(scriptArgs at: index) name -> arg value]].
		
	"Visit the arguments in the correct order"
	scriptArgs do: [:arg | callArgs
		detect: [:kvp | kvp key = arg name]
		ifFound: [:kvp | self visit: kvp value]
		ifNone: ["If we don't find an argument we use the default value"
			(script push: 0) node: node]].
		
	(script call: node scriptName) node: node