receiving
processError
	| errorCode |
	errorCode := self getArgument: serial next or: [^ self].
	Transcript show: 'Uzi error: ';
		show: errorCode;
		cr