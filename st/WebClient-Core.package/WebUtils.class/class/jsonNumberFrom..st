json-decode
jsonNumberFrom: stream
	"Decodes a JSON number from the stream"

	| ascii integer fraction scale sign expSign exponent value ch |
	integer := fraction := 0. sign := scale := exponent := expSign := 1.
	ascii := stream next asciiValue.
	ascii = 45 "$- asciiValue" ifTrue:[
		sign := -1.
		ascii := stream next asciiValue.
	].
	"JSON requires at least one digit"
	(ascii >= 48 and:[ascii <= 57]) ifFalse:[^self error: 'Digit expected'].

	"Read the integer part"
	integer := ascii - 48.
	[ch := stream next ifNil:[^integer * sign].
	ascii := ch asciiValue.
	ascii >= 48 and:[ascii <= 57]] whileTrue:[
		integer := (integer * 10) + (ascii - 48).
	].

	ascii = 46 "$. asciiValue" ifTrue:[
		"Read the fraction part"
		[ch := stream next ifNil:[^(integer * scale + fraction * sign) asFloat / scale].
		ascii := ch asciiValue.
		ascii >= 48 and:[ascii <= 57]] whileTrue:[
			fraction := (fraction * 10) + (ascii - 48).
			scale := scale * 10.
		].
		value := (integer * scale + fraction * sign) asFloat / scale asFloat.
	] ifFalse:[value := integer * sign].

	(ascii =  69 "$E asciiValue" or:[ascii =  101 "$e asciiValue"]) ifTrue:[
		"Read exponent"
		ascii := stream next asciiValue.
		ascii = $- ifTrue:[
			expSign := -1.
			ascii := stream next asciiValue.
		] ifFalse:[ascii = $+ ifTrue:[ascii := stream next asciiValue]].
		exponent := ascii - 48.
		[ch := stream next ifNil:[^value * (10 raisedTo: expSign * exponent)].
		ascii := ch asciiValue.
		ascii >= 48 and:[ascii <= 57]] whileTrue:[
			exponent := (exponent * 10) + (ascii - 48).			
		].
		exponent := exponent * expSign.
	].

	"Skip back before last character since number might be part of a sequence
	like 1, 2, 3, 4, etc (which would eat the trailing comma)"
	ch isAlphaNumeric ifTrue:[^self error: 'Delimiter expected'].
	stream skip: -1.

	exponent = 1 ifFalse:[
		exponent < 0 ifTrue:[value := value asFloat].
		value := value * (10 raisedTo: exponent).
	].
	^value