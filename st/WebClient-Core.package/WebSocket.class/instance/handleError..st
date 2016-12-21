private
handleError: exception
	"Handle an error"

	^self handle: 'error' arguments: (Array with: exception with: self)