visiting
visitScriptCallNode: node
	| callArgs scriptArgs |
	scriptArgs := (ast scriptNamed: node script name) arguments.
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