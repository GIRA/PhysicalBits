decoding
encodeUrlEncodedForm: fieldMap
	"Encodes the fieldsMap in the form of var1=val1&var2=val2.
	The fieldMap can be EITHER an array of associations OR a Dictionary of 
	key value pairs (the former is useful for providing multiple fields and/or 
	specifying the order of fields)."

	| associations |
	associations := fieldMap isDictionary ifTrue:[fieldMap associations] ifFalse:[fieldMap].
	^String streamContents:[:s|
		associations do:[:assoc|
			s nextPutAll: assoc key encodeForHTTP.
			assoc value ifNotNil:[
				s nextPutAll: '='.
				s nextPutAll: assoc value asString encodeForHTTP
			].
		] separatedBy:[s nextPutAll:'&'].
	].