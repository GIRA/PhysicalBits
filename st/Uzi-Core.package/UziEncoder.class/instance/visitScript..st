encoding scripts
visitScript: anUziScript 
	script := anUziScript.
	self writeScriptHeader: anUziScript;
		writeVariables: anUziScript;
		writeInstructions: anUziScript