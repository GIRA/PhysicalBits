visiting
visitScript: anUziScript 
	self writeScriptHeader: anUziScript;
		writeLiterals: anUziScript;
		writeVariables: anUziScript;
		writeBytecodes: anUziScript