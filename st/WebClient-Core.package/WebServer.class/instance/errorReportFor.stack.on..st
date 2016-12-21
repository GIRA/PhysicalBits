errors
errorReportFor: anError stack: preStack on: strm
	"Provide a report for an error that's been caught during processing"

	self errorStackFor: anError stack: preStack on: strm.
	strm cr; nextPutAll: '-----------------------------------------------------------------'; cr; cr.
	self systemInfoOn: strm.
