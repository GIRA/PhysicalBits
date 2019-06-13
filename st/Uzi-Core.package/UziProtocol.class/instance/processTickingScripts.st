receiving
processTickingScripts
	| count values |
	count := serial next.
	values := (1 to: count) collect: [:ign | serial next].
	scriptWatchers := Dictionary new.
	
	currentProgram ifNil: [^ self].
	count ~= currentProgram scripts size ifTrue: [^ self].
	
	values withIndexDo: [:next :index || script isRunning errorCode |
		script := currentProgram scripts at: index.
		isRunning := (next bitAnd: 2r10000000) > 0.
		errorCode := next bitAnd: 2r01111111.
		scriptWatchers
			at: script 
			put: (UziScriptWatcher
				script: script
				isRunning: isRunning
				errorCode: errorCode)]