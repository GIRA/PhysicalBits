receiving
processError
	| errorCode |
	errorCode := serial next.
	Transcript show: 'Uzi error: ';
		show: errorCode;
		cr