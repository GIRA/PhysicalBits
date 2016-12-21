errors
handleError: ex request: aRequest
	"The default error handling during processing. Sends a 500 response."

	| errLabel errReport |
	errLabel := [ex description] on: Error do:[:ex2| 
		ex2 return: ex class name
	].
	errReport := [self errorReportFor: ex] on: Error do:[:ex2|
		ex2 return: '<error report failed>'.
	].
	[self log: errLabel] on: Error do:[:ex2| ex return].
	[self log: String cr, errReport] on: Error do:[:ex2| ex return].
	aRequest send500Response: errReport. 