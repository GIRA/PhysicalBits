fields
getFields
	"Decodes the fields embedded in the url ?var1=val1&var2=val2"

	| args |
	args := (rawUrl copyAfter: $?) ifEmpty:[^Dictionary new].
	^WebUtils decodeUrlEncodedForm: args multipleValues: false
