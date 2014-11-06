accessing
rebuildInterpreters 
	interpreters := program scripts collect: [:script |
		UziScriptInterpreter on: protocol script: script]