encoding scripts
visitScript: anUziScript 
	script := anUziScript.
	self indent; writeLine: 'script: #{1}' format: { anUziScript name };
		indent; writeLine: 'ticking: {1}' format: { anUziScript isTicking };
		indent; writeLine: 'delay: {1}' format: { anUziScript delay value };
		indent; writeLine: 'instructions: [:s | s'.
	self indent: 1 
		during: [
			self writeArguments: anUziScript arguments.
			self writeLocals: anUziScript locals.
			self writeInstructions: anUziScript instructions.
			self write: ']']